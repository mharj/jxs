package lan.sahara.jxs.common;

public class Property {
	private final int _id;
	public int _type;
	public byte _format;
	public byte[] _data = null;

	/**
	 * Constructor.
	 * 
	 * @param id The property's ID.
	 * @param type The ID of the property's type atom.
	 * @param format Data format = 8, 16, or 32.
	 */
	public Property(int id, int type, byte format) {
		_id = id;
		_type = type;
		_format = format;
	}

	/**
	 * Constructor.
	 * 
	 * @param p The property to copy.
	 */
	private Property(final Property p) {
		_id = p._id;
		_type = p._type;
		_format = p._format;
		_data = p._data;
	}
}
