package classes;

import java.util.HashSet;
import java.util.Set;

import core.InvalidInputException;

/**
 * Defines a glyph list and methods to manipulate them. Subclass and define
 * allowed_glyphs as appropriate for your class.
 */
public class Glyphs {

    @SuppressWarnings("serial")
    class InvalidGlyphException extends InvalidInputException {
        public InvalidGlyphException(String message) {
            super(message);
        }
    }

    private Set<String> present_glyphs = new HashSet<String>();;
    private Set<String> allowed_glyphs;

    /**
     * Initializes allowed_glyphs according to game class. Sets the glyphs
     * passed after checking.
     * @param allowed_glyphs2
     * @param args
     */
    public Glyphs(Set<String> allowed_glyphs2, String... args) {
        this.allowed_glyphs = allowed_glyphs2;
        for (String glyph : args) {
            set_glyph(glyph);
        }
    }

    /**
     * Allowed glyphs getter. This is to be used by EP functions mainly.
     * @return The set of allowed glyphs.
     */
    public Set<String> allowed_glyphs() {
        return this.allowed_glyphs;
    }

    /**
     * Throws an exception if the glyph is not allowed.
     * @param glyph A string with the glyph name to be checked.
     */
    public void _check_allowed(String glyph) {
        if (!this.allowed_glyphs.contains(glyph))
            throw new InvalidGlyphException(String.format("%s is not an allowed glyph", glyph));
    }

    /**
     * This checks if a glyph is present.
     * @param glyph A string with the glyph name
     * @return True if present, False if not
     */
    public boolean exists_glyph(String glyph) {
        return present_glyphs.contains(glyph);
    }

    /**
     * This checks if a glyph is present, but returns a 0/1 value.
     * @param glyph A string with the glyph name
     * @return 1 if present, 0 if not
     */
    public int get(String glyph) {
        return (this.present_glyphs.contains(glyph)) ? 1 : 0;
    }

    /**
     * Glyph setter. Checks the validity and appends to preset_glyphs.
     * @param glyph A string with the glyph name.
     */
    public void set_glyph(String glyph) {
        this._check_allowed(glyph);
        present_glyphs.add(glyph);
    }

    /**
     * Glyph deleter. Removes the glyph from present_glyphs.
     * @param glyph A string with the glyph name.
     */
    public void del_glyph(String glyph) {
        present_glyphs.remove(glyph);
    }

    /**
     * This is used by the EP functions to switch the presence of a glyph.
     * @param glyph A string with the glyph name to be switched.
     */
    public void switch_glyph(String glyph) {
        if (this.exists_glyph(glyph))
            this.del_glyph(glyph);
        else
            this.set_glyph(glyph);
    }

}
