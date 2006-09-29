package uk.ac.starlink.ttools.task;

import java.io.IOException;
import java.util.List;
import uk.ac.starlink.table.ColumnInfo;
import uk.ac.starlink.table.ColumnStarTable;
import uk.ac.starlink.table.ConcatStarTable;
import uk.ac.starlink.table.ConstantColumn;
import uk.ac.starlink.table.DefaultValueInfo;
import uk.ac.starlink.table.DescribedValue;
import uk.ac.starlink.table.JoinStarTable;
import uk.ac.starlink.table.StarTable;
import uk.ac.starlink.table.ValueInfo;
import uk.ac.starlink.task.Environment;
import uk.ac.starlink.task.Parameter;
import uk.ac.starlink.task.TaskException;
import uk.ac.starlink.ttools.TableConsumer;

/**
 * TableMapper which concatenates tables top to bottom.
 *
 * @author   Mark Taylor
 * @since    15 Sep 2006
 */
public class CatMapper implements TableMapper {

    private final Parameter seqParam_;
    private final Parameter locParam_;
    private final Parameter ulocParam_;
    private MapperTask task_;

    private static final ValueInfo SEQ_INFO =
        new DefaultValueInfo( "iseq", Short.class,
                              "Sequence number of input table " +
                              "from concatenation operation" );
    private static final ValueInfo LOC_INFO =
        new DefaultValueInfo( "loc", String.class,
                              "Location of input table " +
                              "from concatenation operation" );
    private static final ValueInfo ULOC_INFO =
        new DefaultValueInfo( "uloc", String.class,
                              "Unique part of input table location " +
                              "from concatenation operation" );
    static {
        ((DefaultValueInfo) SEQ_INFO).setNullable( false );
    }

    /**
     * Constructor.
     */
    public CatMapper() {
        seqParam_ = new Parameter( "seqcol" );
        seqParam_.setUsage( "<colname>" );
        seqParam_.setNullPermitted( true );
        seqParam_.setDefault( null );
        seqParam_.setDescription( new String[] {
            "Name of a column to be added to the output table",
            "which will contain the sequence number of the input table",
            "from which each row originated.",
            "This column will contain 1 for the rows from the first",
            "concatenated table, 2 for the second, and so on.",
        } );

        locParam_ = new Parameter( "loccol" );
        locParam_.setUsage( "<colname>" );
        locParam_.setNullPermitted( true );
        locParam_.setDefault( null );
        locParam_.setDescription( new String[] {
            "Name of a column to be added to the output table",
            "which will contain the location",
            "(as specified in the input parameter(s))",
            "of the input table from which each row originated.",
        } );

        ulocParam_ = new Parameter( "uloccol" );
        ulocParam_.setUsage( "<colname>" );
        ulocParam_.setNullPermitted( true );
        ulocParam_.setDefault( null );
        ulocParam_.setDescription( new String[] {
            "Name of a column to be added to the output table",
            "which will contain the unique part of the location",
            "(as specified in the input parameter(s))",
            "of the input table from which each row originated.",
            "If not null, parameters will also be added to the output table",
            "giving the pre- and post-fix string common to all the locations.",
            "For example, if the input tables are \"/data/cat_a1.fits\"",
            "and \"/data/cat_b2.fits\" then the output table will contain",
            "a new column &lt;colname&gt; which takes the value",
            "\"a1\" for rows from the first table and",
            "\"b2\" for rows from the second, and new parameters",
            "\"" + prefixParamName( "&lt;colname&gt;" ) + "\" and",
            "\"" + postfixParamName( "&lt;colname&gt;" ) + "\"",
            "with the values \"/data/cat_\" and \".fits\" respectively.",
        } );
    }

    public Parameter[] getParameters() {
        return new Parameter[] {
            seqParam_,
            locParam_,
            ulocParam_,
        };
    }

    public TableMapping createMapping( Environment env ) throws TaskException {
        String seqCol = seqParam_.stringValue( env );
        String locCol = locParam_.stringValue( env );
        String ulocCol = ulocParam_.stringValue( env );
        return new CatMapping( seqCol, locCol, ulocCol,
                               getInputLocations( env ) );
    }

    /**
     * Sets the task with which this mapper is associated.
     * Required so that we can interrogate it to find out input table
     * locations which are needed unders some circumstances.
     *
     * @param   task  mapper task
     */
    public void setTask( MapperTask task ) {
        task_ = task;
    }

    /**
     * Returns the locations of the input tables for this mapping.
     * The task must be set ({@link #setTask}) for this to work.
     *
     * @param   env  execution environment
     * @return  input table location strings
     */
    private String[] getInputLocations( Environment env ) throws TaskException {
        InputTableSpec[] inSpecs = task_.getInputSpecs( env );
        String[] locs = new String[ inSpecs.length ];
        for ( int i = 0; i < inSpecs.length; i++ ) {
            locs[ i ] = inSpecs[ i ].getLocation();
        }
        return locs;
    }

    /**
     * Name of a parameter to describe the prefix applied to a given column.
     *
     * @param  colName  column name
     * @return  prefix parameter name
     */
    private static String prefixParamName( String colName ) {
        return colName + "_prefix";
    }

    /**
     * Name of a parameter to describe the postfix applied to a given column.
     *
     * @param  colName  column name
     * @return  postfix parameter name
     */
    private static String postfixParamName( String colName ) {
        return colName + "_postfix";
    }

    /**
     * Mapping which concatenates the tables.
     */
    private static class CatMapping implements TableMapping {

        private final String seqCol_;
        private final String locCol_;
        private final String ulocCol_;
        private final String[] locations_;
        private final Trimmer trimmer_;

        /**
         * Constructor.
         *
         * @param  seqCol  name of sequence column to be added, or null
         * @param  locCol  name of location column to be added, or null
         * @param  ulocCol name of unique location to be added, or null
         * @param  locations  location strings for each of the input tables
         *         which will be presented
         */
        CatMapping( String seqCol, String locCol, String ulocCol,
                    String[] locations ) {
            seqCol_ = seqCol;
            locCol_ = locCol;
            ulocCol_ = ulocCol;
            locations_ = locations;
            trimmer_ = ulocCol == null ? null : new Trimmer( locations );
        }

        public void mapTables( StarTable[] inTables, TableConsumer[] consumers )
                throws IOException {
            int nTable = inTables.length;

            /* Work out length of fixed-length columns.  This can prevent
             * an additional pass to find it out for some output modes. */
            int locLeng = 0;
            int ulocLeng = 0;
            for ( int i = 0; i < nTable; i++ ) {
                String loc = locations_[ i ];
                if ( locCol_ != null ) {
                    locLeng = Math.max( locLeng, loc.length() );
                }
                if ( ulocCol_ != null ) {
                    ulocLeng = Math.max( ulocLeng,
                                         trimmer_.trim( loc ).length() );
                }
            }

            /* Append additional columns to the input tables as required. */
            for ( int i = 0; i < nTable; i++ ) {
                final StarTable inTable = inTables[ i ];
                ColumnStarTable addTable = new ColumnStarTable( inTable ) {
                    public long getRowCount() {
                        return inTable.getRowCount();
                    }
                };
                if ( seqCol_ != null ) {
                    ColumnInfo seqInfo = new ColumnInfo( SEQ_INFO );
                    seqInfo.setName( seqCol_ );
                    Short iseq = new Short( (short) ( i + 1 ) );
                    addTable.addColumn( new ConstantColumn( seqInfo, iseq ) );
                }
                if ( locCol_ != null ) {
                    ColumnInfo locInfo = new ColumnInfo( LOC_INFO );
                    locInfo.setName( locCol_ );
                    locInfo.setElementSize( locLeng );
                    String loc = locations_[ i ];
                    addTable.addColumn( new ConstantColumn( locInfo, loc ) );
                }
                if ( ulocCol_ != null ) {
                    ColumnInfo ulocInfo = new ColumnInfo( ULOC_INFO );
                    ulocInfo.setName( ulocCol_ );
                    ulocInfo.setElementSize( ulocLeng );
                    String uloc = trimmer_.trim( locations_[ i ] );
                    addTable.addColumn( new ConstantColumn( ulocInfo, uloc ) );
                }
                if ( addTable.getColumnCount() > 0 ) {
                    inTables[ i ] =
                        new JoinStarTable( new StarTable[] { inTables[ i ],
                                                             addTable } );
                }
            }

            /* Perform the concatenation on the (possibly doctored) input 
             * tables. */
            StarTable out = new ConcatStarTable( inTables[ 0 ], inTables );

            /* Add parameters describing the unique column name truncation
             * if appropriate. */
            if ( ulocCol_ != null ) {
                String preDesc = "String prepended to " + ulocCol_
                               + " column to form source table location";
                String postDesc = "String appended to " + ulocCol_ +
                                  " column to form source table location";
                ValueInfo preInfo =
                    new DefaultValueInfo( prefixParamName( ulocCol_ ), 
                                          String.class, preDesc );
                ValueInfo postInfo = 
                    new DefaultValueInfo( postfixParamName( ulocCol_ ),
                                          String.class, postDesc );
                String pre = trimmer_.getPrefix();
                String post = trimmer_.getPostfix();
                List outParams = out.getParameters();
                if ( pre.trim().length() > 0 ) {
                    outParams.add( new DescribedValue( preInfo, pre ) );
                }
                if ( post.trim().length() > 0 ) {
                    outParams.add( new DescribedValue( postInfo, post ) );
                }
            }

            /* Hand the output table on for processing. */
            consumers[ 0 ].consume( out );
        }
    }

    /**
     * Utility class which identifies common pre- and post-fixes of a 
     * set of strings.
     */
    private static class Trimmer {

        private final String pre_;
        private final String post_;

        /**
         * Constructor.
         *
         * @param   locs  array of strings with the same pre- and post-fixes
         */
        public Trimmer( String[] locs ) {

            /* Find minimum common length. */
            int nloc = locs.length;
            String loc0 = locs[ 0 ];
            int leng = loc0.length();
            for ( int iloc = 0; iloc < nloc; iloc++ ) {
                leng = Math.min( leng, locs[ iloc ].length() );
            }

            /* Find length of maximum common prefix string. */
            int npre = -1;
            for ( int ic = 0; ic < leng && npre < 0; ic++ ) {
                char c = loc0.charAt( ic );
                for ( int iloc = 0; iloc < nloc; iloc++ ) {
                    if ( locs[ iloc ].charAt( ic ) != c ) {
                        npre = ic;
                    }
                }
            }
            pre_ = loc0.substring( 0, npre );

            /* Find length of maximum common postfix string. */
            int npost = -1;
            for ( int ic = 0; ic < leng && npost < 0; ic++ ) {
                char c = loc0.charAt( loc0.length() - 1 - ic );
                for ( int iloc = 0; iloc < nloc; iloc++ ) {
                    if ( locs[ iloc ].charAt( locs[ iloc ].length() - 1 - ic )
                         != c ) {
                        npost = ic;
                    }
                }
            }
            post_ = loc0.substring( loc0.length() - npost );
        }

        /**
         * Returns the common prefix.
         *
         * @return  prefix
         */
        public String getPrefix() {
            return pre_;
        }

        /**
         * Returns the common postfix.
         *
         * @return  postfix
         */
        public String getPostfix() {
            return post_;
        }

        /**
         * Returns a string trimmed of the common pre- and post-fix.
         * Any of the strings submitted to the constructor can be 
         * thus processed without error.
         *
         * @param   loc  input string
         * @return  trimmed string
         * @throws  IllegalArgumentException if loc doesn't have the right form
         */
        public String trim( String loc ) {
            if ( loc.startsWith( pre_ ) && loc.endsWith( post_ ) ) {
                return loc.substring( pre_.length(),
                                      loc.length() - post_.length() );
            }
            else {
                throw new IllegalArgumentException();
            }
        }
    }
}
