package uk.ac.starlink.treeview;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import uk.ac.starlink.table.ColumnHeader;
import uk.ac.starlink.table.StarTable;
import uk.ac.starlink.table.UCD;

/**
 * A MetamapGroup which describes the columns of a StarTable.
 *
 * @author  Mark Taylor (Starlink)
 */
public class StarTableMetamapGroup extends MetamapGroup {

    private static final String INDEX_KEY = "Index";
    private static final String NAME_KEY = "Name";
    private static final String UNITS_KEY = "Units";
    private static final String DESCRIPTION_KEY = "Description";
    private static final String UCD_KEY = "UCD";
    private static final String UCD_DESCRIPTION_KEY = "UCD description";
    private static final List basicOrder = Arrays.asList( new String[] {
        INDEX_KEY, NAME_KEY, UNITS_KEY, DESCRIPTION_KEY, 
        UCD_KEY, UCD_DESCRIPTION_KEY, 
    } );

    public StarTableMetamapGroup( StarTable startable ) {

        /* Superclass constructor. */
        super( startable.getNumColumns() );

        /* Set up the natural ordering for keys. */
        List order = new ArrayList( basicOrder );
        order.addAll( startable.getColumnMetadataKeys() );
        setKeyOrder( order );

        /* Add the metadata for each column. */
        int ncol = startable.getNumColumns();
        for ( int i = 0; i < ncol; i++ ) {
            ColumnHeader colhead = startable.getHeader( i );
            addEntry( i, INDEX_KEY, new Integer( i + 1 ) );
            addEntry( i, NAME_KEY, colhead.getName() );
            addEntry( i, UNITS_KEY, colhead.getUnitString() );
            addEntry( i, UCD_KEY, colhead.getUCD() );
            addEntry( i, DESCRIPTION_KEY, colhead.getDescription() );
            if ( hasEntry( i, UCD_KEY ) ) {
                UCD ucd = UCD.getUCD( (String) getEntry( i, UCD_KEY ) );
                String desc = ( ucd != null ) ? ucd.getDescription()
                                              : "<unknown UCD>";
                addEntry( i, UCD_DESCRIPTION_KEY, desc );
            }
            for ( Iterator it = colhead.metadata().entrySet().iterator();
                  it.hasNext(); ) {
                Map.Entry entry = (Map.Entry) it.next();
                addEntry( i, entry.getKey().toString(), entry.getValue() );
            }
        }
    }
}
