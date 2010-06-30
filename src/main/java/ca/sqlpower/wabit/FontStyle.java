package ca.sqlpower.wabit;

import java.awt.Font;

/**
 * This enum comes from the documentation on {@link Font#decode(String)}
 * to convert from the font style to a string defined in the documentation
 * and back.
 */
public enum FontStyle {
	BOLD("BOLD", Font.BOLD),
	ITALIC("ITALIC", Font.ITALIC),
	PLAIN("PLAIN", Font.PLAIN),
	BOLDITALIC("BOLDITALIC", Font.BOLD + Font.ITALIC);
	
	private final String encodedName;
	private final int value;

	private FontStyle(String encodedName, int value) {
		this.encodedName = encodedName;
		this.value = value;
		
	}

	public String getEncodedName() {
		return encodedName;
	}

	public int getValue() {
		return value;
	}
	
	public static FontStyle getStyleByValue(int value) {
		for (FontStyle style : values()) {
			if (style.getValue() == value) {
				return style;
			}
		}
		throw new IllegalArgumentException("Unknown format for font integer value " + value);
	}
	
}