package uk.ac.starlink.ttools.plot2.config;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;

/**
 * Config key for double precision values.
 * NaN and null are not distinguished.
 *
 * <p>Static methods are provided to produce config keys with a variety of
 * GUI options for specifying values.
 *
 * @author  Mark Taylor
 * @since   22 Feb 2013
 */
public abstract class DoubleConfigKey extends ConfigKey<Double> {

    /**
     * Constructor.
     *
     * @param   meta  metadata
     * @param   dflt  default value
     */
    protected DoubleConfigKey( ConfigMeta meta, double dflt ) {
        super( meta, Double.class, new Double( dflt ) );
        if ( meta.getStringUsage() == null ) {
            meta.setStringUsage( "<number>" );
        }
    }

    public String valueToString( Double value ) {
        return ( value == null || Double.isNaN( value.doubleValue() ) )
             ? ""
             : value.toString();
    }

    public Double stringToValue( String txt ) {
        if ( txt == null || txt.trim().length() == 0 ) {
            return new Double( Double.NaN );
        }
        else {
            try {
                return Double.valueOf( txt.trim() );
            }
            catch ( NumberFormatException e ) {
                throw new ConfigException( this,
                                           "\"" + txt + "\" not numeric", e );
            }
        }
    }

    /**
     * Constructs a key with a text field specifier and default NaN.
     *
     * @param  meta  metadata
     * @return  key
     */
    public static DoubleConfigKey createTextKey( ConfigMeta meta ) {
        return createTextKey( meta, Double.NaN );
    }

    /**
     * Constructs a key with a text field specifier and an explicit default.
     *
     * @param  meta  metadata
     * @param  dflt  default value
     * @return  key
     */
    public static DoubleConfigKey createTextKey( ConfigMeta meta,
                                                 double dflt ) {
        return new DoubleConfigKey( meta, dflt ) {
            public Specifier<Double> createSpecifier() {
                return new TextFieldSpecifier( this, new Double( Double.NaN ) );
            }
        };
    }

    /**
     * Returns a key with a specifier that only provides a
     * toggle between two values.
     *
     * @param   meta  metadata
     * @param   fval  value for toggle false (default)
     * @param   tval  value for toggle true 
     * @return  key
     */
    public static DoubleConfigKey createToggleKey( ConfigMeta meta,
                                                   final double fval,
                                                   final double tval ) {
        return new DoubleConfigKey( meta, fval ) {
            public Specifier<Double> createSpecifier() {
                return new ToggleSpecifier( fval, tval );
            }
        };
    }

    /**
     * Returns a key with a linear or logarithmic slider for a specifier.
     * Note the lower and upper bounds configure only the slider range,
     * they do not enforce a range when the value is set from a string value.
     *
     * @param   meta   metadata
     * @param   dflt   default value
     * @param   lo   slider lower bound
     * @param   hi   slider upper bound
     * @param  log  true for logarithmic slider scale, false for linear
     * @return  key
     */
    public static DoubleConfigKey createSliderKey( ConfigMeta meta,
                                                   final double dflt,
                                                   final double lo,
                                                   final double hi,
                                                   final boolean log ) {
        return new DoubleConfigKey( meta, dflt ) {
            public Specifier<Double> createSpecifier() {
                return new SliderSpecifier( lo, hi, log, dflt );
            }
        };
    }

    /**
     * Check box specifier for double values.
     * One of two floating point values can be chosen from the GUI.
     */
    private static class ToggleSpecifier extends SpecifierPanel<Double> {
        private final Double fVal_;
        private final Double tVal_;
        private final JCheckBox checkBox_;

        /**
         * Constructor.
         *
         * @param  fval  value if unchecked
         * @param  tval  value if checked
         */
        ToggleSpecifier( double fval, double tval ) {
            super( false );
            fVal_ = new Double( fval );
            tVal_ = new Double( tval );
            checkBox_ = new JCheckBox();
        }

        protected JComponent createComponent() {
            checkBox_.addActionListener( getActionForwarder() );
            return checkBox_;
        }

        public Double getSpecifiedValue() {
            return checkBox_.isSelected() ? tVal_ : fVal_;
        }

        public void setSpecifiedValue( Double value ) {
            if ( value == null ) {
                value = new Double( Double.NaN );
            }
            if ( value.equals( fVal_ ) ) {
                checkBox_.setSelected( false );
            }
            else if ( value.equals( tVal_ ) ) {
                checkBox_.setSelected( true );
            }
            fireAction();
        }
    }
}
