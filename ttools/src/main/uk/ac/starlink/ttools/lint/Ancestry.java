package uk.ac.starlink.ttools.lint;

/**
 * Defines the family relationships of an ElementHandler.
 *
 * @author   Mark Taylor (Starlink)
 * @since    7 Apr 2005
 */
public interface Ancestry {

    /**
     * Returns the handler itself.
     *
     * @return  self
     */
    ElementHandler getSelf();

    /**
     * Returns the handler's parent.
     *
     * @return   parent
     */
    ElementHandler getParent();

    /**
     * Returns the handler's nearest ancestor (excluding itself) of a given
     * class.  <tt>clazz</tt> must be ElementHandler or a subclass.
     * If there is no handler in the ancestry of type <tt>clazz</tt>, 
     * null is returned.
     *
     * @param  clazz   class required
     * @return   handler's ancestor of type <tt>clazz</tt>
     */
    ElementHandler getAncestor( Class clazz );

    /**
     * Returns the index of this child in the list of its parent's children.
     * The first child of a parent has index 0.
     *
     * @return   sibling index
     */
    int getSiblingIndex();

    /**
     * Returns the number of child elements this handler currently has.
     *
     * @return  child count
     */
    int getChildCount();
}