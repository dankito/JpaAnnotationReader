package net.dankito.jpa.annotationreader.config;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

/**
 * Data type enumeration that corresponds to a DataPersister.
 *
 * @author graywatson
 */
public enum DataType {

	/**
	 * Persists the {@link String} Java class.
	 */
	STRING(String.class),
	/**
	 * Persists the {@link String} Java class.
	 */
	LONG_STRING(String.class),
	/**
	 * Persists the {@link String} Java class as an array of bytes. By default this will use {@link #STRING} so you will
	 * need to specify this using DatabaseField.dataType().
	 */
	STRING_BYTES(String.class),
	/**
	 * Persists the boolean Java primitive.
	 */
	BOOLEAN(boolean.class),
	/**
	 * Persists the {@link Boolean} object Java class.
	 */
	BOOLEAN_OBJ(Boolean.class),
	/**
	 * Persists the boolean Java primitive as a character in the database.
	 */
	BOOLEAN_CHAR(boolean.class),
	/**
	 * Persists the boolean Java primitive as an integer in the database.
	 */
	BOOLEAN_INTEGER(boolean.class),
	/**
	 * Persists the {@link Date} Java class.
	 * 
	 * <p>
	 * NOTE: This is <i>not</i> the same as the {@link java.sql.Date} class.
	 * </p>
	 */
	DATE(Date.class),

	/**
	 * Persists the {@link Date} Java class as long milliseconds since epoch. By default this will use
	 * {@link #DATE} so you will need to specify this using DatabaseField.dataType().
	 * 
	 * <p>
	 * NOTE: This is <i>not</i> the same as the {@link java.sql.Date} class.
	 * </p>
	 */
	DATE_LONG(Date.class),
	/**
	 * Persists the {@link Date} Java class as a string of a format. By default this will use {@link #DATE} so
	 * you will need to specify this using DatabaseField.dataType().
	 * 
	 * <p>
	 * NOTE: This is <i>not</i> the same as the {@link java.sql.Date} class.
	 * </p>
	 * 
	 * <p>
	 * <b>WARNING:</b> Because of SimpleDateFormat not being reentrant, this has to do some synchronization with every
	 * data in/out unfortunately.
	 * </p>
	 */
	DATE_STRING(Date.class),
	/**
	 * Persists the char primitive.
	 */
	CHAR(char.class),
	/**
	 * Persists the {@link Character} object Java class.
	 */
	CHAR_OBJ(Character.class),
	/**
	 * Persists the byte primitive.
	 */
	BYTE(byte.class),
	/**
	 * Persists the byte[] array type. Because of some backwards compatibility issues, you will need to specify this
	 * using DatabaseField.dataType(). It won't be detected automatically.
	 */
	BYTE_ARRAY(byte[].class),
	/**
	 * Persists the {@link Byte} object Java class.
	 */
	BYTE_OBJ(Byte.class),
	/**
	 * Persists the short primitive.
	 */
	SHORT(short.class),
	/**
	 * Persists the {@link Short} object Java class.
	 */
	SHORT_OBJ(Short.class),
	/**
	 * Persists the int primitive.
	 */
	INTEGER(int.class),
	/**
	 * Persists the {@link Integer} object Java class.
	 */
	INTEGER_OBJ(Integer.class),
	/**
	 * Persists the long primitive.
	 */
	LONG(long.class),
	/**
	 * Persists the {@link Long} object Java class.
	 */
	LONG_OBJ(Long.class),
	/**
	 * Persists the float primitive.
	 */
	FLOAT(float.class),
	/**
	 * Persists the {@link Float} object Java class.
	 */
	FLOAT_OBJ(Float.class),
	/**
	 * Persists the double primitive.
	 */
	DOUBLE(double.class),
	/**
	 * Persists the {@link Double} object Java class.
	 */
	DOUBLE_OBJ(Double.class),
	/**
	 * Persists an unknown Java Object that is serializable. Because of some backwards and forwards compatibility
	 * concerns, you will need to specify this using DatabaseField.dataType(). It won't be detected
	 * automatically.
	 */
	SERIALIZABLE(Serializable.class),
  /**
   * Persists an Enum Java class as its ordinal integer value. You can also specify the {@link #ENUM_STRING} as the
   * type.
   */
  ENUM_INTEGER(Enum.class),
	/**
	 * Persists an Enum Java class as its string value. You can also specify the {@link #ENUM_INTEGER} as the type.
	 */
	ENUM_STRING(Enum.class),
	/**
	 * Persists the {@link java.util.UUID} Java class.
	 */
	UUID(java.util.UUID.class),
	/**
	 * Persists the {@link BigInteger} Java class.
	 */
	BIG_INTEGER(BigInteger.class),
  /**
   * Persists the {@link BigDecimal} Java class as a SQL NUMERIC.
   */
  BIG_DECIMAL_NUMERIC(BigDecimal.class),
	/**
	 * Persists the {@link BigDecimal} Java class as a String.
	 */
	BIG_DECIMAL(BigDecimal.class),
	/**
	 * Persists the org.joda.time.DateTime type with reflection since we don't want to add the dependency. Because this
	 * class uses reflection, you have to specify this using DatabaseField.dataType(). It won't be detected
	 * automatically.
	 */
//	DATE_TIME(Class.forName("joda.time.DateTime"), DateTimeType.getSingleton()),
  DATE_TIME(null),
  /**
	 * Persists the {@link java.sql.Date} Java class.
	 * 
	 * <p>
	 * NOTE: If you want to use the {@link Date} class then use {@link #DATE} which is recommended instead.
	 * </p>
	 */
	SQL_DATE(java.sql.Date.class),
	/**
	 * Persists the {@link java.sql.Timestamp} Java class. The {@link #DATE} type is recommended instead.
	 */
	TIME_STAMP(java.sql.Timestamp.class),
	/**
	 * Marker for fields that are unknown.
	 */
	UNKNOWN(null),
	// end
	;

  private final Class type;

	private DataType(Class type) {
    this.type = type;
	}

  public Class getType() {
    return type;
  }

}
