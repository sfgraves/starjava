package uk.ac.starlink.votable;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import uk.ac.starlink.fits.FitsTableBuilder;
import uk.ac.starlink.table.StarTable;
import uk.ac.starlink.util.DataSource;

/**
 * An object representing the TABLE element of a VOTable.
 * This contains fields, links and rows; the actual data from the table
 * body may be obtained using the {@link #getData} method.
 * Note that depending on exactly how this element was obtained,
 * the nodes bearing the bulk data (e.g. text content of a &lt;STREAM&gt;
 * element or the &lt;TR&gt; children of a &lt;TABLEDATA&gt; element)
 * may not actually be available from this node - for efficiency
 * the VOTable parser may convert them into a {@link TabularData} object
 * and discard the content of the original (STREAM or TABLEDATA) nodes
 * which contained the data from the DOM.
 *
 * @author   Mark Taylor (Starlink)
 */
public class TableElement extends VOElement {

    private TabularData tdata_;
    private static final Logger logger_ = 
        Logger.getLogger( "uk.ac.starlink.votable" );

    /**
     * Constructs a TableElement from a DOM element.
     *
     * @param  base TABLE element
     * @param  doc  owner document for new element
     */
    TableElement( Element base, VODocument doc ) {
        super( base, doc, "TABLE" );
    }

    /**
     * Returns the FIELD elements for this table.  Note these may come
     * from a different TABLE element referenced using this one's
     * <tt>ref</tt> attribute.
     *
     * @return  the FIELD elements which describe the columns of this table
     */
    public FieldElement[] getFields() {
        VOElement[] voels = getMetadata().getChildrenByName( "FIELD" );
        FieldElement[] fels = new FieldElement[ voels.length ];
        System.arraycopy( voels, 0, fels, 0, voels.length );
        return fels;
    }

    /**
     * Returns the LINK elements for this table.
     *
     * @return  the LINK elements which are children of this table
     */
    public LinkElement[] getLinks() {
        VOElement[] voels = getChildrenByName( "LINK" );
        LinkElement[] lels = new LinkElement[ voels.length ];
        System.arraycopy( voels, 0, lels, 0, voels.length );
        return lels;
    }

    /**
     * Returns the PARAM elements for this table.
     *
     * @return  the PARAM elements which are children of this table
     */
    public ParamElement[] getParams() {
        VOElement[] voels = getChildrenByName( "PARAM" );
        ParamElement[] pels = new ParamElement[ voels.length ];
        System.arraycopy( voels, 0, pels, 0, voels.length );
        return pels;
    }

    /**
     * Returns the number of rows in this table.
     * This may be determined from the optional <tt>nrows</tt> attribute
     * or from the table data itself.
     * If this cannot be determined, or cannot be determined efficiently,
     * the value -1 may be returned.
     *
     * @return  the number of rows, or -1 if unknown
     */
    public long getNrows() {
        if ( hasAttribute( "nrows" ) ) {
            String nr = getAttribute( "nrows" );
            try {
                return Long.parseLong( nr );
            }
            catch ( NumberFormatException e ) {
                logger_.warning( "nrows value not an integer: " + nr );
                return -1L;
            }
        }
        else if ( tdata_ != null ) {
            return tdata_.getRowCount();
        }
        else if ( getChildByName( "DATA" ) == null ) {
            return 0L;
        }
        else {
            return -1L;
        }
    }

    /**
     * Returns an object which can be used to access the actual cell
     * data in the body of this table.
     *
     * @return   bulk data access object
     */
    public TabularData getData() throws IOException {
        synchronized ( this ) {
            if ( tdata_ == null ) {
                setData( makeTabularData() );
            }
        }
        return tdata_;
    }

    /**
     * Sets the tabular data object which can be used to access the actual
     * cell data in the body of this table.
     *
     * @param  tdata  tabular data object
     */
    void setData( TabularData tdata ) {
        tdata_ = tdata;
    }

    /**
     * Constructs and returns a TabularData object for this table
     * based on the current contents of the DOM.
     *
     * @return   new TabularData object
     */
    private TabularData makeTabularData() throws IOException {

        /* Get the FIELD elements. */
        FieldElement[] fields = getFields();
        int ncol = fields.length;

        /* Get the associated decoders and content types. */
        final Class[] clazzes = new Class[ ncol ];
        final Decoder[] decoders = new Decoder[ ncol ];
        for ( int i = 0; i < ncol; i++ ) {
            decoders[ i ] = fields[ i ].getDecoder();
            clazzes[ i ] = decoders[ i ].getContentClass();
        }

        /* Get the DATA element. */
        VOElement dataEl = getChildByName( "DATA" );

        /* If there's no DATA we have an empty table (perfectly legal). */
        if ( dataEl == null ) {
            return new TableBodies.EmptyTabularData( clazzes );
        }

        /* Try TABLEDATA format. */
        VOElement tdEl = dataEl.getChildByName( "TABLEDATA" );
        if ( tdEl != null ) {
            return new TableBodies.TabledataTabularData( decoders, tdEl );
        }

        /* Try BINARY format. */
        VOElement binaryEl = dataEl.getChildByName( "BINARY" );
        if ( binaryEl != null ) {
            final VOElement streamEl = binaryEl.getChildByName( "STREAM" );
            if ( streamEl == null ) {
                logger_.warning( "No BINARY/STREAM element" );
                return new TableBodies.EmptyTabularData( clazzes );
            }
            String href = streamEl.getAttribute( "href" );
            if ( href != null && href.length() > 0 ) {
                URL url = getContextURL( href );
                String encoding = streamEl.getAttribute( "encoding" );
                return new TableBodies
                          .HrefBinaryTabularData( decoders, url, encoding );
            }
            else {
                return new TableBodies.SequentialTabularData( clazzes ) {
                    public RowStepper getRowStepper() throws IOException {
                        InputStream istrm = getTextChildrenStream( streamEl );
                        return new BinaryRowStepper( decoders, istrm,
                                                     "base64" );
                    }
                };
            }
        }

        /* Try FITS format. */
        VOElement fitsEl = dataEl.getChildByName( "FITS" );
        if ( fitsEl != null ) {
            String extnum = fitsEl.getAttribute( "extnum" );
            if ( extnum != null && extnum.trim().length() == 0 ) {
                extnum = null;
            }
            final VOElement streamEl = fitsEl.getChildByName( "STREAM" );
            if ( streamEl == null ) {
                logger_.warning( "No FITS/STREAM element" );
                return new TableBodies.EmptyTabularData( clazzes );
            }
            String href = streamEl.getAttribute( "href" );

            /* Construct a DataSource which knows how to retrieve the
             * input stream corresponding to the FITS data.  Ignore the
             * stated encoding here, since DataSource can work out
             * compression formats on its own. */
            DataSource datsrc;
            if ( href != null && href.length() > 0 ) {
                URL url = getContextURL( href );
                datsrc = DataSource.makeDataSource( url );
            }
            else {
                datsrc = new DataSource() {
                    protected InputStream getRawInputStream() {
                        return new BufferedInputStream(
                                   new Base64InputStream(
                                       getTextChildrenStream( streamEl ) ) );
                    }
                };
                datsrc.setName( "STREAM" );
            }
            datsrc.setPosition( extnum );
            StarTable startab =
                new FitsTableBuilder()
               .makeStarTable( datsrc, false, 
                               ((VODocument) getOwnerDocument())
                              .getStoragePolicy() );
            if ( startab == null ) {
                throw new IOException(
                    "STREAM element does not contain a FITS table" );
            }
            return new TableBodies.StarTableTabularData( startab );
        }

        /* We don't seem to have any data. */
        logger_.warning( "DATA element contains none of " +
                         "TABLEDATA, FITS or BINARY" );
        return new TableBodies.EmptyTabularData( clazzes );
    }

    /**
     * Returns the element which provides the metadata (FIELD elements -
     * and other things?  the standard isn't too clear) for this table.
     * This is usually this element itself, but it
     * might be one referenced by a <tt>ref</tt> element, or it might
     * be a dummy empty element if the one referenced by a ref element
     * doesn't exist.
     *
     * @return   FIELD-bearing description element of this table
     */
    private TableElement getMetadata() {
        if ( hasAttribute( "ref" ) ) {
            String ref = getAttribute( "ref" );
            Node node = getOwnerDocument().getElementById( ref );
            if ( node instanceof TableElement ) {
                return (TableElement) node;
            }
            else {
                logger_.warning( "No TABLE element is referenced by ID "
                               + ref );
                return (TableElement)
                       getOwnerDocument().createElement( "TABLE" );
            }
        }
        else {
            return this;
        }
    }

    /**
     * Returns the concatenated content of all the text-type children of
     * a given node as an input stream.
     *
     * @param   node  node whose children we are interested in
     * @return  concatenation of textual data as a stream
     */
    private static InputStream getTextChildrenStream( Node node ) {
        List tchildren = new ArrayList();
        for ( Node child = node.getFirstChild(); child != null;
              child = child.getNextSibling() ) {
            if ( child instanceof Text ) {
                tchildren.add( child );
            }
        }
        final Enumeration en = Collections.enumeration( tchildren );
        return new SequenceInputStream( new Enumeration() {
            public boolean hasMoreElements() {
                return en.hasMoreElements();
            }
            public Object nextElement() {
                Text textNode = (Text) en.nextElement();
                String text = textNode.getData();
                InputStream tstrm = new StringInputStream( text );
                return tstrm;
            }
        } );
    }

    /**
     * Utility class which reads a String as an InputStream.
     * Normally this isn't a respectable thing to do because of encodings,
     * but this is used for base64-encoded strings, so we know that a
     * simple cast of char to byte is the right way to do the conversion.
     */
    static class StringInputStream extends InputStream {
        private final String text;
        private final int leng;
        private int pos = 0;
        private int marked = 0;
        public StringInputStream( String text ) {
            this.text = text;
            this.leng = text.length();
        }
        public int available() {
            return leng - pos;
        }
        public int read() {
            return pos < leng ? (int) text.charAt( pos++ )
                              : -1;
        }
        public int read( byte[] b, int off, int size ) {
            int i = 0;
            for ( ; i < size && pos < leng; i++ ) {
                b[ off++ ] = (byte) text.charAt( pos++ );
            }
            return i;
        }
        public int read( byte[] b ) {
            return read( b, 0, b.length );
        }
        public long skip( long n ) {
            long i = Math.min( n, (long) ( leng - pos ) );
            pos += (int) i;
            return i;
        }
        public void mark( int limit ) {
            marked = pos;
        }
        public void reset() {
            pos = marked;
        }
        public boolean markSupported() {
            return true;
        }
    }

}
