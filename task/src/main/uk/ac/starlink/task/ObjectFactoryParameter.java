package uk.ac.starlink.task;

import uk.ac.starlink.util.LoadException;
import uk.ac.starlink.util.ObjectFactory;

/**
 * Parameter whose (user-supplied) string values correspond to nicknames
 * from an {@link ObjectFactory}.  The {@link #objectValue} method returns
 * the corresponding object generated by the factory for that nickname.
 *
 * @author   Mark Taylor
 * @since    22 Nov 2006
 */
public class ObjectFactoryParameter<T> extends Parameter<T> {

    private final ObjectFactory<T> factory_;
    private Object objValue_;

    /**
     * Constructor.
     *
     * @param  name  parameter name
     * @param  factory   object factory
     */
    public ObjectFactoryParameter( String name, ObjectFactory<T> factory ) {
        super( name, factory.getFactoryClass(), false );
        factory_ = factory;
    }

    /**
     * Returns the object factory used by this parameter.
     *
     * @return  object factory
     */
    public ObjectFactory<T> getObjectFactory() {
        return factory_;
    }

    public T stringToObject( Environment env, String sval )
            throws ParameterValueException {
        try {
            return factory_.createObject( sval );
        }
        catch ( LoadException e ) {
            throw new ParameterValueException( this, e );
        }
    }

    @Override
    public String getUsage() {
        StringBuffer sbuf = new StringBuffer();
        String[] nickNames = factory_.getNickNames();
        for ( int i = 0; i < nickNames.length; i++ ) {
            if ( i > 0 ) {
                sbuf.append( '|' );
            }
            sbuf.append( nickNames[ i ] );
        }
        return sbuf.toString();
    }
}
