#!/usr/bin/perl -w

use strict;

use JMaker;
use SrcReader;

my( $cName ) = "AstObject";

my( $fName );
my( $aName );

print "package uk.ac.starlink.ast;\n\n";
print "import java.io.IOException;\n\n";

makeClassHeader( 
   Name => $cName,
   purpose => ClassPurpose( $cName ),
   descrip => ClassDescrip( $cName ),
   version => '$Id$',
   author => "Mark Taylor (Starlink)",
);

print "public class AstObject {\n";

print <<'__EOT__';

    /** Holds the C pointer to the AST object.  Used by native code. */
    protected long pointer = 0;

    /** Bad coordinate value. */
    public static double AST__BAD;

    /* This call performs the native static initialisation required before
     * any of the AST native methods can work.  Since all the AST objects
     * are subclassed from this one, putting it in the static initialisation
     * of this class guarantees it will be done before any other native
     * methods are called. */
    static {
        System.loadLibrary( "jniast" );
        nativeInitialize();
        AST__BAD = getAstConstantD( "AST__BAD" );
    }

    /**
     * Dummy constructor.  This constructor does not create a valid
     * AstObject object, but is required for inheritance by AstObject's
     * subclasses.
     */
    protected AstObject() {
    }

    /**
     * Finalize the object; this annuls the AST object to free resources.
     * Subclasses which override this method should call it in their
     * finalizers.
     */
    protected void finalize() throws Throwable {
        if ( pointer != 0 ) {
            annul();
        }
        pointer = 0;
        super.finalize();
    }

    /* Initializer for shared library. */
    private native static void nativeInitialize();

    /**
     * Gets the value of a named integer constant from the underlying
     * AST library.
     *
     * @param  constname  the name of the constant ("AST__<i>something</i>")
     * @return the value of <code>constname</code>
     */
    static native int getAstConstantI( String constname );

    /**
     * Gets the value of a named double precision constant from the underlying
     * AST library.
     *
     * @param  constname  the name of the constant ("AST__<i>something</i>")
     * @return the value of <code>constname</code>
     */
    static native double getAstConstantD( String constname );

    /**
     * Annul this object.  Associated resources in the underlying library
     * are reclaimed.  Following this call the object cannot be used.
     * User code should not normally call this, but should dereference
     * the object instead (the <code>finalize</code> method calls
     * <code>annul</code>.
     */
    public native void annul();

    /**
     * Delete this object.  Associated resources in the underlying library
     * are reclaimed, and <i>any</i> remaining references to the
     * underlying object are rendered invalid.  
     * <p>
     * Note that deletion is unconditional, regardless of other references
     * to this java object or to the underlying AST object.  This method
     * should be used with caution.
     */
    public native void delete();

    /**
     * Clone an object.
     * This method creates a new Object which is a reference to the 
     * same underlying AST object.  This method calls the astClone
     * function of the underlying AST library and makes a new AstObject
     * referencing the result.
     *
     * @return  a new <code>AstObject</code> containing a reference to the 
     *          underlying AST object referenced by this <code>AstObject</code>
     * @see  AstObject#sameObject(Object) sameObject
     */
    public native Object clone();

    /**
     * Determine whether two AstObjects are similar in all respects.
     * This method is implemented by writing both objects to a 
     * {@link Channel} and comparing the resulting textual representations.
     * It may therefore be relatively expensive.
     *
     * @param   obj  object to be compared with this one
     * @return       true if <code>obj</code> resembles this object in all
     *               respects, false otherwise
     * @see  AstObject#sameObject(Object) sameObject
     */
    public boolean equals( Object obj ) {
        boolean eq = false;
        if ( obj.getClass().equals( getClass() ) ) {
            AstStringer stringer = new AstStringer();
            String s1 = stringer.representation( this );
            String s2 = stringer.representation( (AstObject) obj );
            eq = ( s1 != null ) && ( s2 != null ) && s1.equals( s2 );
        } 
        return eq;
    }

    /**
     * Return a hash code for this AstObject.  The notion of equality
     * in this method must match the one in the equals method.
     * This method is implemented by writing the object to a 
     * {@link Channel} and calculating the hash code of the resulting
     * String.  It may therefore be relatively expensive.
     *
     * @return  the hash code of this AstObject
     */
    public int hashCode() {
        String s = new AstStringer().representation( this );
        if ( s != null ) {
            return s.hashCode();
        }

        /* There shouldn't be an error, but if there is just use the 
         * same hashcode implementation used by Object. */
        else {
            return System.identityHashCode( this );
        }
    }

    /**
     * Determine whether two AstObjects are references to the same 
     * underlying object.  Since AstObjects are merely containers for
     * references to objects in the underlying AST library, 
     * two AstObjects may effectively be references to the same object
     * (so that, for instance, changing an attribute of one will change
     * it in the other) but the <code>==</code> operator applied between
     * them will return false.  This method can tell you whether two
     * AstObjects refer to the same thing.
     *
     * @param  obj   an object to be compared to this one.
     * @return       true if <code>obj</code> references the same AST object
     *               as this does.
     * @see          AstObject#equals(Object) equals
     */
    public boolean sameObject( Object obj ) {
        boolean same = false;
        if ( obj instanceof AstObject ) {
            AstObject aobj = (AstObject) obj;
            String oldID = getID();
            if ( oldID.equals( aobj.getID() ) ) {
                String newID = oldID + "x";
                setID( newID );
                if ( newID.equals( aobj.getID() ) ) {
                    same = true;
                }
                if ( oldID.length() == 0 ) {
                    clear( "ID" );
                }
                else {
                    setID( oldID );
                }
            }
        }
        return same;
    }

    /* Local class used by the equals and hashCode methods. */
    private class AstStringer extends Channel {
        StringBuffer buf = new StringBuffer();
        {
            setFull( -1 );
            setComment( false );
        }
        protected void sink( String line ) {
            buf.append( line + "\n" );
        }
        synchronized String representation( AstObject ao ) {
            try {
                write( ao );
                String result = buf.toString();
                buf.setLength( 0 );
                return result;
            }
            catch ( IOException e ) {
                return null;
            }
        }
    }


__EOT__


makeNativeMethod(
   name => ( $fName = "clear" ),
   purpose => FuncPurpose( $fName ),
   descrip => FuncDescrip( $fName ),
   return => { type => 'void', },
   params => [
      { 
         name => ( $aName = "attrib" ),
         type => 'String',
         descrip => ArgDescrip( $fName, $aName ),
      },
   ],
);

makeNativeMethod(
   name => ( $fName = "copy" ),
   purpose => FuncPurpose( $fName ),
   descrip => FuncDescrip( $fName ),
   params => [],
   return => {
      type => 'AstObject',
      descrip => ReturnDescrip( $fName ),
   },
);


my( $Xtype );
foreach $Xtype (
   [ "C", "String", "character" ],
   [ "D", "double", "double precision" ],
   [ "F", "float", "floating point" ],
   [ "L", "long", "long integer" ],
   [ "I", "int", "integer" ],
   [ "B", "boolean", "boolean" ],
) {
   my( $Xletter, $Xjtype, $Xcomm ) = @{$Xtype};

   if ( $Xjtype eq "boolean" ) {
      print <<__EOT__;
    /**
     * Get a boolean attribute value by name.  This is a convenience
     * method which calls <code>getI</code> but maps integers to 
     * booleans.
     *
     * \@param   attrib     the name of the boolean attribute to retrieve
     * \@return  the named attribute as a <code>boolean</code>
     * \@throws  AstException  if the AST routine generates an error,
     *                        in particular if no integer attribute by
     *                        this name exists
     */
    public boolean getB( String attrib ) {
        return ( getI( attrib ) != 0 );
    }

    /**
     * Set a boolean attribute value by name.  This is a convenience
     * method which calls <code>setI</code> but maps <code>boolean</code>
     * values to integers.
     *
     * \@param   attrib         the name of the boolean attribute to set
     * \@param   value          the new value of <code>attrib</code>
     * \@throws  AstException   if the AST routine generates an error,
     *                         in particular if no writable integer
     *                         attribute by that name exists
     */
    public void setB( String attrib, boolean value ) {
        setI( attrib, ( value ? 1 : 0 ) );
    }

__EOT__

   }
   else {
      print <<__EOT__;
    /**
     * Get a $Xcomm attribute value by name.
     *
     * \@param  attrib  the name of the $Xcomm attribute to retrieve
     * \@return the named attribute as a <code>$Xjtype</code>
     * \@throws AstException  if the AST routine generates an error, in 
     *                       particular if no $Xcomm 
     *                       attribute called <code>attrib</code> exists
     */
    public native $Xjtype get$Xletter( String attrib );

    /**
     * Set a $Xcomm attribute value by name.
     *
     * \@param  attrib the name of the $Xcomm attribute to set
     * \@param  value  the new value of the attribute
     * \@throws AstException  if the AST routine generates an error, 
     *                       in particular if no writable $Xcomm
     *                       attribute called <code>attrib</code> exists
     */
    public native void set$Xletter( String attrib, $Xjtype value );

__EOT__

   }
}

makeNativeMethod( 
   name => ( $fName = "set" ),
   purpose => FuncPurpose( $fName ),
   descrip => FuncDescrip( $fName ),
   return => { type => 'void' },
   params => [
      {
         name => ( $aName = "settings" ),
         type => 'String',
         descrip => ArgDescrip( $fName, $aName ),
      },
   ],
);


makeNativeMethod(
   name => ( $fName = "show" ),
   purpose => FuncPurpose( $fName ),
   descrip => FuncDescrip( $fName ),
   params => [],
   return => { type => 'void' },
);

makeNativeMethod(
   name => ( $fName = "test" ),
   purpose => FuncPurpose( $fName ),
   descrip => FuncDescrip( $fName ),
   return => { type => 'boolean' },
   params => [
      {
         name => ( $aName = "attrib" ),
         type => 'String',
         descrip => ArgDescrip( $fName, $aName ),
      },
   ],
);


my( @args );

@args = (
   name => ( $aName = "ID" ),
   type => 'String',
   purpose => AttPurpose( $aName ),
   descrip => AttDescrip( $aName ),
);
makeGetAttrib( @args );
makeSetAttrib( @args );

@args = (
   name => ( $aName = "Ident" ),
   type => 'String',
   purpose => AttPurpose( $aName ),
   descrip => AttDescrip( $aName ),
);
makeGetAttrib( @args );
makeSetAttrib( @args );

@args = (
   name => ( $aName = "nobject" ),
   type => 'int',
   purpose => AttPurpose( $aName ),
   descrip => AttPurpose( $aName ),
);
makeGetAttrib( @args );

@args = (
   name => ( $aName = "refCount" ),
   type => 'int',
   purpose => AttPurpose( $aName ),
   descrip => AttDescrip( $aName ),
);
makeGetAttrib( @args );


print "}\n";


