package uk.ac.starlink.ttools.taplint;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Enumerates known ReportCode instances.
 * A short description is provided for each instance.
 * Note the description is not definitive, the actual message passed
 * through the reporting system in association with the code is the one
 * that should actually be passed to the user.
 *
 * <p>This class exists for taplint clients that want to have a static
 * idea of the ReportCode instances that may be reported by the taplint
 * framework.  Note it does <em>not</em> provide a complete list of
 * all the ReportCodes, since other ReportCode sublclasses may be used.
 * But it is expected to be the large majority.
 *
 * <p>The name of each enum element has a fixed form, <code>T_LLLL</code>,
 * where <code>T</code> is code.getType().getChar()
 *   and <code>LLLL</code> is code.getLabel().
 *
 * @author   Mark Taylor
 * @since    11 Jun 2014
 */
public enum FixedCode implements ReportCode {

    E_A2XI( "Non-ADQL2 declared as ADQL2" ),
    E_ADQX( "ADQL not declared" ),
    E_BAPH( "Incorrect starting phase" ),
    E_BMIM( "Illegal MIME type" ),
    E_CAIO( "Error reading capabilities metadata" ),
    E_CAXM( "SAX error parsing capabilities metadata" ),
    E_CERR( "Error reading TAP_SCHEMA.columns data" ),
    E_CINT( "Wrong type for TAP_SCHEMA.columns column" ),
    E_CLDR( "Declared columns absent in result" ),
    E_CLIO( "Error reading TAP_SCHEMA.columns table" ),
    E_CLOG( "Non-boolean value" ),
    E_CLRD( "Unexpected columns in result" ),
    E_CNAM( "Query/result column name mismatch" ),
    E_CPIO( "Capabilities metadata read error" ),
    E_CPSX( "Capabilities metadata parse failure" ),
    E_CTYP( "Declared/result type mismatch" ),
    E_CUCD( "UCD mismatch" ),
    E_CUNI( "Unit mismatch" ),
    E_CUTP( "Utype mismatch" ),
    E_DCER( "Document read error" ),
    E_DEHT( "Bad HTTP job connection" ),
    E_DEMO( "Job deletion failure" ),
    E_DENO( "Non-404 response for deleted job" ),
    E_DEOP( "Unavailable job URL" ),
    E_DFIO( "Duff query failed" ),
    E_DFSF( "Duff query result parse failure" ),
    E_DNST( "Missing QUERY_STATUS from duff query" ),
    E_DQUS( "Unknown value for QUERY_STATUS" ),
    E_ELDF( "Wrong top-level element" ),
    E_EST1( "Multiple QUERY_STATUS INFOs" ),
    E_EURL( "HTTP error" ),
    E_FLIO( "Table metadata read error" ),
    E_FLSX( "Table metadata parse failure" ),
    E_FKIO( "Error reading TAP_SCHEMA.keys table" ),
    E_FKLK( "Foreign key link broken" ),
    E_FKNT( "Foreign key target table absent" ),
    E_GEOX( "Unknown geometry function" ),
    E_GMIM( "Content-Type mismatch" ),
    E_GONM( "Missing mandatory resource" ),
    E_HNUL( "Illegal NULLs in ObsCore column" ),
    E_HTDE( "HTTP DELETE failure" ),
    E_HTOF( "Unavailable job URL" ),
    E_IFMT( "Non-integer result" ),
    E_ILOP( "ObsCore value not in required set" ),
    E_ILPH( "Illegal job phase" ),
    E_IOER( "Error reading document" ),
    E_JBIO( "Job read error" ),
    E_JBSP( "Job parse error" ),
    E_JDDE( "Destruction time mismatch" ),
    E_JDED( "Execution duration mismatch" ),
    E_JDID( "Job ID mismatch" ),
    E_JDIO( "Error reading job document" ),
    E_JDNO( "Missing job document" ),
    E_JDPH( "Info/phase phase mismatch" ),
    E_JDSX( "Error parsing job document" ),
    E_KCIO( "Error reading TAP_SCHEMA.key_columns table" ),
    E_KEYX( "Unknown standard language feature key" ),
    E_LVER( "Some ADQL variants fail" ),
    E_MCOL( "Query/result column count mismatch" ),
    E_MUPM( "Missing mandatory upload declaration" ),
    E_NFND( "Non-OK HTTP response" ),
    E_NO11( "Multi-cell result for COUNT" ),
    E_NOHT( "Non-HTTP job URL" ),
    E_NONM( "Non-numeric value for COUNT" ),
    E_NOOF( "No output formats defined" ),
    E_NOQL( "No query languages declared" ),
    E_NOST( "Missing QUERY_STATUS" ),
    E_NREC( "Maxrec limit exceeded" ),
    E_NRER( "COUNT error" ),
    E_NROW( "Row limit exceeded" ),
    E_OCOL( "Missing required ObsCore column" ),
    E_OVNO( "Overflow not signalled" ),
    E_PADU( "Duplicate parameter" ),
    E_PAMM( "Bad job parameter value" ),
    E_PANO( "Nameless parameter" ),
    E_PANZ( "Non-blank job parameter" ),
    E_PHUR( "Incorrect phase" ),
    E_POER( "POST failure" ),
    E_PORE( "POST error" ),
    E_QERR( "Query failed" ),
    E_QERX( "Query result parse error" ),
    E_QFAA( "Query failure" ),
    E_QST1( "Multiple pre-table QUERY_STATUS INFOs" ),
    E_QST2( "Multiple post-table QUERY_STATUS INFOs" ),
    E_QTYP( "Query/result column type mismatch" ),
    E_RANG( "ObsCore values out of range" ),
    E_RDPH( "Job phase read failure" ),
    E_RRES( "RESOURCE not marked 'results'" ),
    E_RRTO( "Row limit exceeded" ),
    E_RUPH( "Bad phase for started job" ),
    E_TBIO( "Error reading TAP_SCHEMA.tables table" ),
    E_TCAP( "Missing TAPRegExt capability element" ),
    E_TMCD( "Upload result column value mismatch" ),
    E_TMCN( "Upload result column name mismatch" ),
    E_TMNC( "Upload result column count mismatch" ),
    E_TMNR( "Upload result row count mismatch" ),
    E_TTOO( "Multiple result TABLEs" ),
    E_TYPX( "ObsCore datatype incompatibilty" ),
    E_UDFE( "Bad UDF declaration" ),
    E_UPBD( "Unknown upload suffix" ),
    E_UPER( "Upload error" ),
    E_VOCT( "Bad Content-Type for VOTable" ),
    E_VTIO( "Query result read error" ),
    E_VVLO( "VOTable version unsupported by TAP" ),
    E_XOFK( "Unknown standard output format" ),

    W_A2MN( "No ADQL2 declaration" ),
    W_A2MX( "Non-standard ADQL2 declaration" ),
    W_AD2X( "ADQL2 not declared" ),
    W_CCAS( "Capitalisation mismatch" ),
    W_CEUK( "Unknown Content-Encoding" ),
    W_CEZZ( "Compression" ),
    W_CIDX( "Index flag mismatch" ),
    W_CLUN( "Columns content" ),
    W_CPID( "Non-IdentifierURI dataModel" ),
    W_CTYP( "Datatype mismatch" ),
    W_CUCD( "UCD mismatch" ),
    W_CULF( "Custom language feature type" ),
    W_CUNI( "Unit mismatch" ),
    W_CUTP( "UType mismatch" ),
    W_DQU2( "Unknown value for post-table QUERY_STATUS" ),
    W_DSUC( "Non-error return from duff query" ),
    W_FKUN( "Keys content" ),
    W_FLUN( "Key columns content" ),
    W_FTYP( "Foreign key type mismatch" ),
    W_GONO( "Missing optional resource" ),
    W_HSTB( "Non-duff return from duff query" ),
    W_HURL( "Redirect to non-HTTP URL" ),
    W_IODM( "Incorrect ObsCore ID" ),
    W_LVAN( "Language has empty version string" ),
    W_NOCT( "Missing Content-Type header" ),
    W_NOMS( "No error message text" ),
    W_NSOP( "ObsCore value not in suggested set" ),
    W_RDIO( "Resource read error" ),
    W_TFMT( "Non-ISO-8601 result" ),
    W_TSDL( "Bad time format in table data" ),
    W_TYPI( "ObsCore datatype mismatch" ),
    W_UNSC( "Foreign schema used in validation" ),
    W_UNPH( "UNKNOWN phase" ),
    W_UPCS( "Custom upload method" ),
    W_WODM( "Incorrect ObsCore ID" ),
    W_ZRES( "Resolver not used?" ),

    F_CAIO( "Bad capabilities service URL" ),
    F_CAPC( "XML parser error" ),
    F_DTIO( "Unexpected table read error" ),
    F_GONE( "Table metadata absent" ),
    F_INTR( "Interrupted" ),
    F_MURL( "Bad URL" ),
    F_NOTB( "No obscore table" ),
    F_NOTM( "Earlier metadata stages not completed" ),
    F_NOUP( "No upload methods listed" ),
    F_SXER( "Unexpected SAX parse error" ),
    F_TIOF( "Error reading result table" ),
    F_TRND( "Table randomisation failure" ),
    F_UTF8( "Unknown UTF8 encoding" ),
    F_XENT( "DTD entity trouble" ),
    F_XURL( "Bad document URL" ),
    F_XVAL( "Validator preparation error" ),
    F_ZCOL( "No columns known for tests" ),

    I_CJOB( "Job created" ),
    I_CURL( "Reading capability metadata" ),
    I_DUFF( "Duff query" ),
    I_JOFI( "Job finished immediately" ),
    I_NODM( "No ObsCore tests" ),
    I_OCCP( "No ObsCore tests" ),
    I_POPA( "Job POSTed" ),
    I_QGET( "Query GET URL" ),
    I_QJOB( "Submitted query URL" ),
    I_QSUB( "Submitted query" ),
    I_SCHM( "Use TAP_SCHEMA for metadata" ),
    I_TMAX( "Table test count" ),
    I_TURL( "Reading table metadata" ),
    I_VURL( "Validation" ),
    I_VVNL( "Undeclared VOTable version" ),
    I_VVUN( "Unknown VOTable version" ),

    S_COLS( "ObsCore columns" ),
    S_FLGO( "Non-standard column flags" ),
    S_FLGS( "Standard column flags" ),
    S_QNUM( "Query count" ),
    S_QTIM( "Query time" ),
    S_SUMM( "Table metadata" ),
    S_VALI( "Validation" );

    private final String description_;

    /**
     * Constructor.
     *
     * @param  description  short description
     */
    FixedCode( String description ) {
        description_ = description;
    }

    public ReportType getType() {
        return ReportType.forChar( name().charAt( 0 ) );
    }

    public String getLabel() {
        return name().substring( 2 );
    }

    /**
     * Returns a short textual description of the use of this code.
     * It may not be very precise; if the message put through the reporting
     * system is available, that should be used in preference.
     *
     * @return  description
     */
    public String getDescription() {
        return description_;
    }
}
