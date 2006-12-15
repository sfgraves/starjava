/*
 * Copyright (C) 2006 Particle Physics and Astronomy Research Council
 *
 *  History:
 *     01-DEC-2006 (Peter W. Draper):
 *       Original version.
 */

package uk.ac.starlink.splat.plot;

import diva.canvas.AbstractFigure;
import diva.canvas.interactor.Interactor;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Date;
import java.text.DecimalFormat;

import javax.swing.JViewport;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import nom.tam.fits.FitsDate;

import uk.ac.starlink.ast.Frame;
import uk.ac.starlink.ast.FrameSet;
import uk.ac.starlink.ast.SpecFrame;
import uk.ac.starlink.ast.DSBSpecFrame;
import uk.ac.starlink.diva.DrawLabelFigure;
import uk.ac.starlink.diva.DrawRectangleFigure;
import uk.ac.starlink.splat.ast.ASTJ;
import uk.ac.starlink.splat.data.SpecData;
import uk.ac.starlink.splat.util.SplatException;

/**
 * A Figure that describes some of the meta-data properties of a
 * spectrum. These are modelled on those liked by the JCMT observers
 * at the JAC (basically those displayed by Specx). The Figure is
 * really a {@link DrawLabelFigure}, but one which retains a fixed position
 * inside a {@link JViewport}, so that it remains visible.
 *
 * @author Peter W. Draper
 * @version $Id$
 */
public class JACSynopsisFigure
    extends DrawLabelFigure
{
    /** The SpecData instance */
    private SpecData specData = null;

    /** The JViewport we're at a fixed position within */
    private JViewport viewport = null;

    /** The underlying location within viewport */
    private Point anchor = null;

    /** The default font */
    private static Font defaultFont = new Font( null, Font.PLAIN, 10 );

    /**
     * Create an instance.
     *
     * @param specData the {@link SpecData} instance.
     * @param viewport the {@link JViewport} that this figure is being
     *                 displayed within.
     * @param anchor the initial position within the {@link JViewport}.
     *               This will change as the figure is dragged around.
     */
    public JACSynopsisFigure( SpecData specData, JViewport viewport,
                              Point anchor )
    {
        super( "JACSynopsisFigure", defaultFont, 4.0,
               SwingConstants.NORTH_WEST );
        this.anchor = anchor.getLocation();
        setSpecData( specData );
        setViewport( viewport );
        reAnchor();
    }

    /**
     * Set the SpecData instance.
     */
    public void setSpecData( SpecData specData )
    {
        this.specData = specData;
        updateProperties();
        fireChanged();
    }

    /**
     * Set the {@link JViewport} instance. The position of our synopsis text
     * is initially fixed within the visible portion of this.
     */
    public void setViewport( JViewport viewport )
    {
        this.viewport = viewport;

        // We want to know when this changes so we can redraw the text at the
        // correct position so register for any changes.
        viewport.addChangeListener( new ChangeListener()
            {
                public void stateChanged( ChangeEvent e )
                {
                    reAnchor();
                }
            });

        fireChanged();
    }

    /**
     * Return a copy of the current local anchor position.
     */
    public Point getLocalAnchor()
    {
        return anchor.getLocation();
    }

    //  Create the String that represents the synopsis.
    protected void updateProperties()
    {
        if ( specData == null ) {
            return;
        }

        ASTJ astj = specData.getAst();
        Frame specAxis = astj.pickAxis( 1 );

        StringBuffer b = new StringBuffer();

        //  SPLAT name.
        b.append( "Name: " + specData.getShortName() + "\n" );

        //  Telescope/instrument/backend.
        String prop = specData.getProperty( "TELESCOP" );
        String telescope = prop;
        if ( ! "".equals( prop ) ) {
            b.append( "Telescope: " + prop );
            prop = specData.getProperty( "INSTRUME" );
            if ( ! "".equals( prop ) ) {
                b.append( " / " + prop );
                prop = specData.getProperty( "BACKEND" );
                if ( ! "".equals( prop ) ) {
                    b.append( " / " + prop );
                }
            }
            b.append( "\n" );
        }

        //  Prefer human readable date of observation. Epoch will
        //  be MJD-OBS.
        prop = specData.getProperty( "DATE-OBS" );
        if ( "".equals( prop ) ) {
            b.append( "Epoch: " + specAxis.getEpoch() + "\n" );
        }
        else {
            b.append( "Date obs: " + prop + " (UTC)\n" );
        }

        //  Target of observation.
        prop = specData.getProperty( "OBJECT" );
        if ( ! "".equals( prop ) ) {
            b.append( "Object: " + prop );

            //  ACSIS specific.
            prop = specData.getProperty( "MOLECULE" );
            if ( ! "".equals( prop ) && ! "No Line".equals( prop ) ) {
                b.append( " / " + prop );
            }
            prop = specData.getProperty( "TRANSITI" );
            if ( ! "".equals( prop ) && ! "No Line".equals( prop ) ) {
                b.append( " / " + prop );
            }
            b.append( "\n" );
        }

        //  Elevation.
        prop = specData.getProperty( "ELSTART" );
        if ( ! "".equals( prop ) ) {
            double elstart = Double.parseDouble( prop );
            prop = specData.getProperty( "ELEND" );
            if ( ! "".equals( prop ) ) {
                double elend = Double.parseDouble( prop );
                b.append( "Elevation: " + (0.5*(elstart+elend)) + "\n" );
            }
            else {
                b.append( "Elevation: " + elstart + "\n" );
            }
        }

        //  Exposure time. These are human readable dates so need to use FITS
        //  parsing.
        prop = specData.getProperty( "INT_TIME" );
        if ( ! "".equals( prop ) ) {
            b.append( "Exposure: " + prop + " (sec)\n" );
        }
        else {
            //  Try elapsed time.
            prop = specData.getProperty( "DATE-OBS" );
            if ( ! "".equals( prop ) ) {
                try {
                    Date dstart = new FitsDate( prop ).toDate();
                    prop = specData.getProperty( "DATE-END" );
                    if ( ! "".equals( prop ) ) {
                        Date dend = new FitsDate( prop ).toDate();

                        //  Get milliseconds in UNIX time.
                        long istart = dstart.getTime();
                        long iend = dend.getTime();
                        b.append( "Exposure (elapsed): "
                                  +( (iend-istart)/1000.0 )+ " (sec)\n" );
                    }
                }
                catch (Exception e) {
                    //  Unparsable time. Just give up.
                }
            }
        }

        b.append( "Coord Sys: " + specAxis.getSystem() + "\n" );
        //b.append( "ObsLat: " + specAxis.getC( "ObsLat" ) + "\n" );
        //b.append( "ObsLon: " + specAxis.getC( "ObsLon" ) + "\n" );

        if ( specAxis instanceof SpecFrame ) {

            //  RA and Dec are ideally from GAIA.
            prop = specData.getProperty( "EXRA" );
            if ( ! "".equals( prop ) ) {
                b.append( "Obs Centre: " +
                          prop + ", " +
                          specData.getProperty( "EXDEC" ) + "\n" );

                //  Extraction offsets from centre.
                prop = specData.getProperty( "EXRAOF" );
                if ( ! "".equals( prop ) ) {
                    b.append( "Ex Offset: " +
                              prop + ", " +
                              specData.getProperty( "EXDECOF" ) +
                              " (arcsec)\n" );
                }
                else {
                    //  No offsets, maybe an extraction position instead
                    //  (non-celestial coords).
                    prop = specData.getProperty( "EXRAX" );
                    if ( ! "".equals( prop ) ) {
                        b.append( "Ex Position: " +
                                  prop + ", " +
                                  specData.getProperty( "EXDECX" ) + "\n" );
                    }
                }
            }
            else {

                //  Just report SpecFrame reference position. This should be
                //  for the source.
                b.append( "RefRA, RefDec: " + specAxis.getC( "RefRA" ) );
                b.append( ", " + specAxis.getC( "RefDec" ) + "\n" );
            }
            b.append( "SourceVel: " + specAxis.getC( "SourceVel" ) + "\n" );
            b.append( "SourceVRF: " + specAxis.getC( "SourceVRF" ) + "\n" );
            b.append( "StdOfRest: " + specAxis.getC( "StdOfRest" ) + "\n" );
            b.append( "RestFreq: " + specAxis.getC( "RestFreq" ) + " (GHz)\n");
        }
        if ( specAxis instanceof DSBSpecFrame ) {
            b.append( "ImagFreq: " + specAxis.getC( "ImagFreq" ) + " (GHz)\n");
            //b.append( "DSBCentre: " + specAxis.getC( "DSBCentre" ) + "\n" );
            //b.append( "IF: " + specAxis.getC( "IF" ) + " (GHz)\n" );
            prop = specData.getProperty( "IFCHANSP" );
            if ( ! "".equals( prop ) ) {
                double inc = Double.parseDouble( prop ) / 1.0E6;
                b.append( "Channel spacing: " + inc + " (MHz)\n" );
            }
        }

        //  Temperature calculations are broken for now, so development mode
        //  only...
        java.util.Properties props = System.getProperties();
        String isDevelop = props.getProperty( "splat.development" );
        if (  isDevelop != null && isDevelop.equals( "1" )  ) {

            //  TSYS and TRX.
            prop = specData.getProperty( "TSYS" );
            if ( "".equals( prop ) ) {
                //  No TSYS, only proceed if have variances and this is the
                //  JCMT.
                if ( "JCMT".equals( telescope ) ) {
                    if ( specData.haveYDataErrors() ) {
                        prop = calculateTsys( specData );
                    }
                }
            }

            if ( ! "".equals( prop ) ) {
                b.append( "TSYS: " + prop + " (K)\n" );
            }

            prop = specData.getProperty( "TRX" );
            if ( ! "".equals( prop ) ) {
                b.append( "TRX: " + prop + " (K)\n" );
            }
        }

        setString( b.toString() );
    }

    /**
     * Reset the text anchor position so that the position within the viewport
     * is retained.
     */
    protected void reAnchor()
    {
        Point p = viewport.getViewPosition();
        p.translate( anchor.x, anchor.y );

        Point2D a = getAnchorPoint();
        translate( p.getX() - a.getX(), p.getY() - a.getY(), false );
    }

    // Don't want this to transform, needs to retain its size, not grow in
    // scale with the other DrawFigure instances.
    public void transform( AffineTransform at )
    {
        // Do nothing.
    }

    //  Like translate, but with optional reset of local anchor position
    //  (do after interactive drag, but not when viewport updates).
    public void translate( double x, double y, boolean reset )
    {
        //  Cheat big time. Use transform() of super-class, not translate.
        //  That avoids having a transform in this class, which is where the
        //  translation request end up.
        super.transform( AffineTransform.getTranslateInstance( x, y ) );

        if ( reset ) {
            //  Reset local anchor wrt to viewport.
            Point2D a = getAnchorPoint();
            Point p = viewport.getViewPosition();
            anchor.setLocation( (int)(a.getX() - p.getX()),
                                (int)(a.getY() - p.getY()) );
        }
    }

    //  Normal translate.
    public void translate( double x, double y )
    {
        //  Reset anchor as should be from an interactive movement.
        translate( x, y, true );
    }

    /**
     * Calculate an estimate for the TSYS.
     */
    protected String calculateTsys( SpecData specData )
    {
        String result = "";

        //  Need the channel spacing and the efficiency factor.
        String prop = specData.getProperty( "BEDEGFAC" );
        if ( "".equals( prop ) ) {
            return result;
        }
        double K = Double.parseDouble( prop );

        prop = specData.getProperty( "IFCHANSP" );
        if ( "".equals( prop ) ) {
            return result;
        }
        double dnu = Math.abs( Double.parseDouble( prop ) );

        //  This is the wrong value, need the spectrum integration time, not
        //  the time for the whole "exposure".
        prop = specData.getProperty( "INT_TIME" );
        if ( "".equals( prop ) ) {
            return result;
        }
        double exposure = Double.parseDouble( prop );

        double[] errors = specData.getYDataErrors();
        int n = errors.length;
        double sum = 0.0;
        int count = 0;
        for ( int i = 0; i < n; i++ ) {
            if ( errors[i] != SpecData.BAD ) {
                sum += ( errors[i] * errors[i] );
                count++;
            }
        }
        if ( count > 0 ) {
            double sigma = Math.sqrt ( sum / (double) count );

            //  I make this from Jamie's original paper:
            // double tsys =
            //    0.5 * sigma * Math.sqrt( 2.0 * dnu * exposure / K );

            //  SpecX says:
            // double tsys = 0.5 * sigma * Math.sqrt( dnu * exposure );
            //

            //  Tim says:
            //
            //  Effective exposure time depends on observing mode.
            //  These corrections are a guess.
            //
            //  Really need some property that has the effective temperature
            //  already calculated.

            //  Hack, exposure only correct for Tim's data.
            exposure = 2.0;

            String sam_mode = specData.getProperty( "SAM_MODE" );
            String sw_mode = specData.getProperty( "SW_MODE" );
            System.out.println( "sam_mode = " + sam_mode );
            System.out.println( "sw_mode = " + sw_mode );
            double teff = exposure;
            if ( sam_mode.equals( "raster" ) ) {
                // toff = sqrt(N)*ton. Need number of ons... Where's that kept?
                double N = 1.0;
                teff = exposure * Math.sqrt(N) / ( 1.0  + Math.sqrt(N) );
            }
            else if ( sw_mode.equals( "chop" ) ) {
                //  toff = ton
                teff = 0.5 * exposure;
            }
            else if ( sw_mode.equals( "pssw" ) ) {
                //  toff = ton
                teff = 0.5 * exposure;
            }
            else if ( sw_mode.equals( "freq" ) ) {
                //  teff = ton, no toff.
                teff = exposure;
            }
            else if ( sw_mode.equals( "none" ) ) {
                //  No toff?
                teff = exposure;
            }

            double tsys = sigma * Math.sqrt( dnu * teff ) / K;

            DecimalFormat f  = new DecimalFormat( "###.##" );
            result = f.format( tsys ) + "(Est) ";
        }
        return result;
    }
}
