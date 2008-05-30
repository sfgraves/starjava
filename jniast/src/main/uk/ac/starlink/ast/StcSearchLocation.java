/* ********************************************************
 * This file automatically generated by StcSearchLocation.pl.
 *                   Do not edit.                         *
 **********************************************************/

package uk.ac.starlink.ast;


/**
 * Java interface to the AST StcSearchLocation class
 *  - correspond to the IVOA SearchLocation class. 
 * The StcSearchLocation class is a sub-class of Stc used to describe 
 * the coverage of a query.
 * <p>
 * See http://hea-www.harvard.edu/~arots/nvometa/STC.html
 * 
 * 
 * @see  <a href='http://star-www.rl.ac.uk/cgi-bin/htxserver/sun211.htx/?xref_StcSearchLocation'>AST StcSearchLocation</a>  
 */
public class StcSearchLocation extends Stc {

   /**
    * Constructs a new StcSearchLocation.
    *
    * @param   region  the encapsulated region
    * @param   coords  the AstroCoords elements associated with this Stc
    */
   public StcSearchLocation( Region region, AstroCoords[] coords ) {
       construct( region, astroCoordsToKeyMaps( coords ) );
   }
   private native void construct( Region region, KeyMap[] coordMaps );
}