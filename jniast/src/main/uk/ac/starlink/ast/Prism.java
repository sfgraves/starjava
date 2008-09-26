/* ********************************************************
 * This file automatically generated by Prism.pl.
 *                   Do not edit.                         *
 **********************************************************/

package uk.ac.starlink.ast;


/**
 * Java interface to the AST Prism class
 *  - an extrusion of a region into higher dimensions. 
 * A Prism is a Region which represents an extrusion of an existing Region 
 * into one or more orthogonal dimensions (specified by an Interval or
 * Box). If the Region to be extruded has N axes, and the Interval or Box 
 * defining the extrusion has M axes, then the resulting Prism will have 
 * (M+N) axes. A point is inside the Prism if the first N axis values 
 * correspond to a point which is inside the Region being extruded, and the 
 * remaining M axis values correspond to a point which inside the supplied 
 * Interval or Box.
 * <p>
 * As an example, a cylinder can be represented by extruding an existing 
 * Circle. In this case the supplied Interval would have a single axis and 
 * would specify the upper and lower limits of the cylinder along its 
 * length.
 * 
 * 
 * @see  <a href='http://star-www.rl.ac.uk/cgi-bin/htxserver/sun211.htx/?xref_Prism'>AST Prism</a>  
 */
public class Prism extends Region {
    /** 
     * Create a Prism.   
     * This function creates a new Prism and optionally initialises
     * its attributes.
     * <p>
     * A Prism is a Region which represents an extrusion of an existing Region 
     * into one or more orthogonal dimensions (specified by an Interval or
     * Box). If the Region to be extruded has N axes, and the Interval or Box 
     * defining the extrusion has M axes, then the resulting Prism will have 
     * (M+N) axes. A point is inside the Prism if the first N axis values 
     * correspond to a point which is inside the Region being extruded, and the 
     * remaining M axis values correspond to a point which inside the supplied 
     * Interval or Box.
     * <p>
     * As an example, a cylinder can be represented by extruding an existing 
     * Circle. In this case the supplied Interval would have a single axis and 
     * would specify the upper and lower limits of the cylinder along its 
     * length.
     * <h4>Notes</h4>
     * <br> - Deep copies are taken of the supplied Regions. This means that
     * any subsequent changes made to the component Regions using the 
     * supplied pointers will have no effect on the Prism.
     * <br> - A null Object pointer (AST__NULL) will be returned if this
     * function is invoked with the AST error status set, or if it
     * should fail for any reason.
     * @param  region1  Pointer to the Region to be extruded.
     * 
     * @param  region2  Pointer to the Interval or Box defining the extent of the extrusion.
     * 
     * @throws  AstException  if an error occurred in the AST library
    */
    public Prism( Region region1, Region region2 ) {
        construct( region1, region2 );
    }
    private native void construct( Region region1, Region region2 );

}