package net.sf.j2s.ajax;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.sf.j2s.annotation.J2SIgnore;
import net.sf.j2s.annotation.J2SKeep;

/**
 * @author zhou renjian
 *
 * 2006-10-11
 */
public class SimpleSerializable implements Cloneable {

    public static SimpleSerializable UNKNOWN = new SimpleSerializable();

    public static boolean BYTES_COMPACT_MODE = false;

    /**
	 * @return
	 * 
	 * @j2sNative
var baseChar = 'B'.charCodeAt (0);
var buffer = [];
buffer[0] = "WLL201";
var oClass = this.getClass();
var clazz = oClass;
var clazzName = clazz.getName();
var idx = -1;
while ((idx = clazzName.lastIndexOf('$')) != -1) {
	if (clazzName.length > idx + 1) {
		var ch = clazzName.charCodeAt (idx + 1);
		if (ch < 48 || ch >= 58) { // not a number!
			break; // inner class
		} 
	}
	clazz = clazz.getSuperclass();
	if (clazz == null) {
		break;
	}
	clazzName = clazz.getName();
}
buffer[1] = clazzName;
buffer[2] = '#';
buffer[3] = "00000000$";
var headSize = buffer.join ('').length;

var fields = oClass.declared$Fields;
if (fields == null) {
	fields = [];
}
var filter = arguments[0];
var ignoring = (filter == null || filter.ignoreDefaultFields ());
var fMap = this.fieldMapping ();
for (var i = 0; i < fields.length; i++) {
	var field = fields[i];
	var name = field.name;
	if (filter != null && !filter.accept (name)) continue;
	var fName = name;
	if (fMap != null && fMap.length > 1) {
		for (var j = 0; j < fMap.length / 2; j++) {
			if (name == fMap[j + j]) {
				var newName = fMap[j + j + 1];
				if (newName != null && newName.length > 0) {
					fName = newName;
				}
				break;
			}
		}
	}
	var nameStr = String.fromCharCode (baseChar + fName.length) + fName;
	var type = field.type;
	if (type == 'F' || type == 'D' || type == 'I' || type == 'L'
			|| type == 'S' || type == 'B' || type == 'b') {
		if (ignoring && this[name] == 0
				&& (type == 'F' || type == 'D' || type == 'I'
				|| type == 'L' || type == 'S' || type == 'B')) {
			continue;
		}
		if (ignoring && this[name] == false && type == 'b') {
			continue;
		}
		buffer[buffer.length] = nameStr;
		buffer[buffer.length] = type;
		var value = null;
		if (type == 'b') {
			value = (this[name] == true) ? "1" : "0";
		} else {
			value = "" + this[name];
		}
		buffer[buffer.length] = String.fromCharCode (baseChar + value.length);
		buffer[buffer.length] = value;
	} else if (type == 'C') {
		if (ignoring && this[name] == 0 || this[name] == '\0') {
			continue;
		}
		buffer[buffer.length] = nameStr;
		buffer[buffer.length] = type;
		var value = "";
		if (typeof this[name] == 'number') {
			value += this[name];
		} else {
			value += this[name].charCodeAt (0);
		}
		buffer[buffer.length] = String.fromCharCode (baseChar + value.length);
		buffer[buffer.length] = value;
	} else if (type == 's') {
		if (ignoring && this[name] == null) {
			continue;
		}
		buffer[buffer.length] = nameStr;
		this.serializeString(buffer, this[name]);
	} else if (type.charAt (0) == 'A') {
		if (this[name] == null) {
			if (ignoring) {
				continue;
			}
			buffer[buffer.length] = nameStr;
			buffer[buffer.length] = String.fromCharCode (baseChar - 1);
		} else {
			buffer[buffer.length] = nameStr;
			buffer[buffer.length] = type;
			var l4 = this[name].length;
			if (l4 > 52) {
				if (l4 > 0x1000000) { // 16 * 1024 * 1024
					throw new RuntimeException("Array size reaches the limit of Java2Script Simple RPC!");
				}
				buffer[buffer.length] = String.fromCharCode (baseChar - 2);
				var value = "" + l4;
				buffer[buffer.length] = String.fromCharCode (baseChar + value.length);
				buffer[buffer.length] = l4;
			} else {
				buffer[buffer.length] = String.fromCharCode (baseChar + l4);
			}
			var t = type.charAt (1);
			var arr = this[name];
			for (var j = 0; j < arr.length; j++) {
				if (t == 'F' || t == 'D' || t == 'I' || t == 'L'
						|| t == 'S' || t == 'B' || t == 'b') {
					var value = null;
					if (type == 'b') {
						value = (arr[j] == true) ? "1" : "0";
					} else {
						value = "" + arr[j];
					}
					buffer[buffer.length] = String.fromCharCode (baseChar + value.length);
					buffer[buffer.length] = value;
				} else if (t == 'C') {
					var value = "";
					if (typeof arr[j] == 'number') {
						value += arr[j];
					} else {
						value += arr[j].charCodeAt (0);
					}
					buffer[buffer.length] = String.fromCharCode (baseChar + value.length);
					buffer[buffer.length] = value;
				} else if (t == 'X') {
					this.serializeString(buffer, arr[j]);
				}
			}
		}
	}
}
var strBuf = buffer.join ('');
var size = strBuf.length; 
if (size > 0x1000000) { // 16 * 1024 * 1024
	throw new RuntimeException("Data size reaches the limit of Java2Script Simple RPC!");
}
var sizeStr = "" + (size - headSize);
strBuf = strBuf.substring (0, headSize - sizeStr.length - 1) + sizeStr + strBuf.substring(headSize - 1);
return strBuf;
	 */
    public String serialize() {
        return serialize(null);
    }

    @J2SIgnore
    public String serialize(SimpleFilter filter) {
        char baseChar = 'B';
        StringBuffer buffer = new StringBuffer();
        buffer.append("WLL201");
        Class<?> clazz = this.getClass();
        String clazzName = clazz.getName();
        int idx = -1;
        while ((idx = clazzName.lastIndexOf('$')) != -1) {
            if (clazzName.length() > idx + 1) {
                char ch = clazzName.charAt(idx + 1);
                if (ch < '0' || ch > '9') {
                    break;
                }
            }
            clazz = clazz.getSuperclass();
            if (clazz == null) {
                break;
            }
            clazzName = clazz.getName();
        }
        buffer.append(clazzName);
        buffer.append('#');
        buffer.append("00000000$");
        int headSize = buffer.length();
        Set<Field> fieldSet = new HashSet<Field>();
        clazz = this.getClass();
        while (clazz != null && !"net.sf.j2s.ajax.SimpleSerializable".equals(clazz.getName())) {
            Field[] fields = clazz.getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                fieldSet.add(fields[i]);
            }
            clazz = clazz.getSuperclass();
        }
        boolean ignoring = (filter == null || filter.ignoreDefaultFields());
        String[] fMap = fieldMapping();
        try {
            Field[] fields = fieldSet.toArray(new Field[0]);
            for (int i = 0; i < fields.length; i++) {
                Field field = fields[i];
                int modifiers = field.getModifiers();
                if ((modifiers & (Modifier.PUBLIC | Modifier.PROTECTED)) != 0 && (modifiers & Modifier.TRANSIENT) == 0 && (modifiers & Modifier.STATIC) == 0) {
                    String name = field.getName();
                    if (filter != null && !filter.accept(name)) continue;
                    if (fMap != null && fMap.length > 1) {
                        for (int j = 0; j < fMap.length / 2; j++) {
                            if (name.equals(fMap[j + j])) {
                                String newName = fMap[j + j + 1];
                                if (newName != null && newName.length() > 0) {
                                    name = newName;
                                }
                                break;
                            }
                        }
                    }
                    String nameStr = (char) (baseChar + name.length()) + name;
                    Class<?> type = field.getType();
                    if (type == float.class) {
                        float f = field.getFloat(this);
                        if (f == 0.0 && ignoring) continue;
                        buffer.append(nameStr);
                        buffer.append('F');
                        String value = "" + f;
                        buffer.append((char) (baseChar + value.length()));
                        buffer.append(f);
                    } else if (type == double.class) {
                        double d = field.getDouble(this);
                        if (d == 0.0d && ignoring) continue;
                        buffer.append(nameStr);
                        buffer.append('D');
                        String value = "" + d;
                        buffer.append((char) (baseChar + value.length()));
                        buffer.append(d);
                    } else if (type == int.class) {
                        int n = field.getInt(this);
                        if (n == 0 && ignoring) continue;
                        buffer.append(nameStr);
                        buffer.append('I');
                        String value = "" + n;
                        buffer.append((char) (baseChar + value.length()));
                        buffer.append(n);
                    } else if (type == long.class) {
                        long l = field.getLong(this);
                        if (l == 0L && ignoring) continue;
                        buffer.append(nameStr);
                        buffer.append('L');
                        String value = "" + l;
                        buffer.append((char) (baseChar + value.length()));
                        buffer.append(l);
                    } else if (type == short.class) {
                        short s = field.getShort(this);
                        if (s == 0 && ignoring) continue;
                        buffer.append(nameStr);
                        buffer.append('S');
                        String value = "" + s;
                        buffer.append((char) (baseChar + value.length()));
                        buffer.append(s);
                    } else if (type == byte.class) {
                        byte b = field.getByte(this);
                        if (b == 0 && ignoring) continue;
                        buffer.append(nameStr);
                        buffer.append('B');
                        String value = "" + b;
                        buffer.append((char) (baseChar + value.length()));
                        buffer.append(b);
                    } else if (type == char.class) {
                        int c = 0 + field.getChar(this);
                        if (c == 0 && ignoring) continue;
                        buffer.append(nameStr);
                        buffer.append('C');
                        String value = "" + c;
                        buffer.append((char) (baseChar + value.length()));
                        buffer.append(c);
                    } else if (type == boolean.class) {
                        boolean b = field.getBoolean(this);
                        if (b == false && ignoring) continue;
                        buffer.append(nameStr);
                        buffer.append('b');
                        String value = b ? "1" : "0";
                        buffer.append((char) (baseChar + value.length()));
                        buffer.append(value);
                    } else if (type == String.class) {
                        String s = (String) field.get(this);
                        if (s == null && ignoring) continue;
                        buffer.append(nameStr);
                        serializeString(buffer, s);
                    } else {
                        if (type == float[].class) {
                            float[] fs = (float[]) field.get(this);
                            if (fs == null && ignoring) continue;
                            buffer.append(nameStr);
                            buffer.append("AF");
                            if (fs == null) {
                                buffer.append((char) (baseChar - 1));
                            } else {
                                serializeLength(buffer, fs.length);
                                for (int j = 0; j < fs.length; j++) {
                                    float f = fs[j];
                                    String value = "" + f;
                                    buffer.append((char) (baseChar + value.length()));
                                    buffer.append(f);
                                }
                            }
                        } else if (type == double[].class) {
                            double[] ds = (double[]) field.get(this);
                            if (ds == null && ignoring) continue;
                            buffer.append(nameStr);
                            buffer.append("AD");
                            if (ds == null) {
                                buffer.append((char) (baseChar - 1));
                            } else {
                                serializeLength(buffer, ds.length);
                                for (int j = 0; j < ds.length; j++) {
                                    double d = ds[j];
                                    String value = "" + d;
                                    buffer.append((char) (baseChar + value.length()));
                                    buffer.append(d);
                                }
                            }
                        } else if (type == int[].class) {
                            int[] ns = (int[]) field.get(this);
                            if (ns == null && ignoring) continue;
                            buffer.append(nameStr);
                            buffer.append("AI");
                            if (ns == null) {
                                buffer.append((char) (baseChar - 1));
                            } else {
                                serializeLength(buffer, ns.length);
                                for (int j = 0; j < ns.length; j++) {
                                    int n = ns[j];
                                    String value = "" + n;
                                    buffer.append((char) (baseChar + value.length()));
                                    buffer.append(n);
                                }
                            }
                        } else if (type == long[].class) {
                            long[] ls = (long[]) field.get(this);
                            if (ls == null && ignoring) continue;
                            buffer.append(nameStr);
                            buffer.append("AL");
                            if (ls == null) {
                                buffer.append((char) (baseChar - 1));
                            } else {
                                serializeLength(buffer, ls.length);
                                for (int j = 0; j < ls.length; j++) {
                                    long l = ls[j];
                                    String value = "" + l;
                                    buffer.append((char) (baseChar + value.length()));
                                    buffer.append(l);
                                }
                            }
                        } else if (type == short[].class) {
                            short[] ss = (short[]) field.get(this);
                            if (ss == null && ignoring) continue;
                            buffer.append(nameStr);
                            buffer.append("AS");
                            if (ss == null) {
                                buffer.append((char) (baseChar - 1));
                            } else {
                                serializeLength(buffer, ss.length);
                                for (int j = 0; j < ss.length; j++) {
                                    short s = ss[j];
                                    String value = "" + s;
                                    buffer.append((char) (baseChar + value.length()));
                                    buffer.append(s);
                                }
                            }
                        } else if (type == byte[].class) {
                            byte[] bs = (byte[]) field.get(this);
                            if (bs == null && ignoring) continue;
                            buffer.append(nameStr);
                            buffer.append(!bytesCompactMode() ? "AB" : "A8");
                            if (bs == null) {
                                buffer.append((char) (baseChar - 1));
                            } else {
                                serializeLength(buffer, bs.length);
                                if (!bytesCompactMode()) {
                                    for (int j = 0; j < bs.length; j++) {
                                        byte b = bs[j];
                                        String value = "" + b;
                                        buffer.append((char) (baseChar + value.length()));
                                        buffer.append(b);
                                    }
                                } else {
                                    buffer.append(new String(bs, "iso-8859-1"));
                                }
                            }
                        } else if (type == char[].class) {
                            char[] cs = (char[]) field.get(this);
                            if (cs == null && ignoring) continue;
                            buffer.append(nameStr);
                            buffer.append("AC");
                            if (cs == null) {
                                buffer.append((char) (baseChar - 1));
                            } else {
                                serializeLength(buffer, cs.length);
                                for (int j = 0; j < cs.length; j++) {
                                    int c = cs[j];
                                    String value = "" + c;
                                    buffer.append((char) (baseChar + value.length()));
                                    buffer.append(c);
                                }
                            }
                        } else if (type == boolean[].class) {
                            boolean[] bs = (boolean[]) field.get(this);
                            if (bs == null && ignoring) continue;
                            buffer.append(nameStr);
                            buffer.append("Ab");
                            if (bs == null) {
                                buffer.append((char) (baseChar - 1));
                            } else {
                                serializeLength(buffer, bs.length);
                                for (int j = 0; j < bs.length; j++) {
                                    boolean b = bs[j];
                                    String value = b ? "1" : "0";
                                    buffer.append((char) (baseChar + value.length()));
                                    buffer.append(value);
                                }
                            }
                        } else if (type == String[].class) {
                            String[] ss = (String[]) field.get(this);
                            if (ss == null && ignoring) continue;
                            buffer.append(nameStr);
                            buffer.append("AX");
                            if (ss == null) {
                                buffer.append((char) (baseChar - 1));
                            } else {
                                serializeLength(buffer, ss.length);
                                for (int j = 0; j < ss.length; j++) {
                                    String s = ss[j];
                                    serializeString(buffer, s);
                                }
                            }
                        } else {
                            continue;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        int size = buffer.length();
        if (size > 0x1000000) {
            throw new RuntimeException("Data size reaches the limit of Java2Script Simple RPC!");
        }
        String sizeStr = "" + (size - headSize);
        buffer.replace(headSize - sizeStr.length() - 1, headSize - 1, sizeStr);
        return buffer.toString();
    }

    @J2SIgnore
    private void serializeLength(StringBuffer buffer, int length) {
        char baseChar = 'B';
        if (length > 52) {
            if (length > 0x1000000) {
                throw new RuntimeException("Array size reaches the limit of Java2Script Simple RPC!");
            }
            buffer.append((char) (baseChar - 2));
            String value = "" + length;
            buffer.append((char) (baseChar + value.length()));
            buffer.append(length);
        } else {
            buffer.append((char) (baseChar + length));
        }
    }

    /**
	 * @param buffer
	 * @param s
	 * @throws UnsupportedEncodingException
	 * 
	 * @j2sNative
var baseChar = 'B'.charCodeAt (0);
if (s == null) {
	buffer[buffer.length] = 's';
	buffer[buffer.length] = String.fromCharCode (baseChar - 1);
} else {
	var normal = /^[\r\n\t -~]*$/.test(s);
	if (normal) {
		buffer[buffer.length] = 's';
	} else {
		buffer[buffer.length] = 'u';
		s = Encoding.encodeBase64 (Encoding.convert2UTF8 (s));
	}
	var l4 = s.length;
	if (l4 > 52) {
		buffer[buffer.length] = String.fromCharCode (baseChar - 2);
		var value = "" + l4;
		buffer[buffer.length] = String.fromCharCode (baseChar + value.length);
		buffer[buffer.length] = l4;
	} else {
		buffer[buffer.length] = String.fromCharCode (baseChar + l4);
	}
	buffer[buffer.length] = s;
}
	 */
    @J2SKeep
    private void serializeString(StringBuffer buffer, String s) throws UnsupportedEncodingException {
        char baseChar = 'B';
        if (s != null) {
            byte[] bytes = s.getBytes("utf-8");
            if (s.length() != bytes.length) {
                buffer.append('u');
                s = Base64.byteArrayToBase64(bytes);
            } else {
                buffer.append('s');
            }
            int l4 = s.length();
            if (l4 > 52) {
                buffer.append((char) (baseChar - 2));
                String value = "" + l4;
                buffer.append((char) (baseChar + value.length()));
                buffer.append(l4);
            } else {
                buffer.append((char) (baseChar + l4));
            }
            buffer.append(s);
        } else {
            buffer.append('s');
            buffer.append((char) (baseChar - 1));
        }
    }

    /**
	 * @param str
	 * @return whether given string is deserialized as expected or not
	 * 
	 * @j2sNative
var start = 0;
if (arguments.length == 2) {
	start = arguments[1];
}
var baseChar = 'B'.charCodeAt (0);
if (str == null || start < 0) return false;
var length = str.length - start;
if (length <= 7 || str.substring(start, start + 3) != "WLL") return false;
var index = str.indexOf('#', start);
if (index == -1) return false;
index++;
if (index >= length + start) return false; // may be empty string!

var size = 0;
var nextCharCode = str.charCodeAt(index);
if (nextCharCode >= 48 && nextCharCode <= 57) {
	var last = index;
	index = str.indexOf('$', last);
	if (index == -1) return false;
	var sizeStr = str.substring(last + 1, index);
	sizeStr = sizeStr.replace(/^0+/, '');
	if (sizeStr.length != 0) {
		try {
			size = parseInt(sizeStr);
		} catch (e) { }
	}
	// all fields are in their default values or no fields
	if (size == 0) return true;
	index++;
	if (size > length + start - index) return false; 
}

var fieldMap = [];
var fields = this.getClass ().declared$Fields;
if (fields != null) {
	for (var i = 0; i < fields.length; i++) {
		var field = fields[i];
		var name = field.name;
		fieldMap[name] = true;
	}
}
var end = index + size;
var fMap = this.fieldMapping ();
while (index < start + length && index < end) {
	var c1 = str.charCodeAt (index++);
	var l1 = c1 - baseChar;
	if (l1 < 0) return true;
	var fieldName = str.substring (index, index + l1);
	if (fMap != null && fMap.length > 1) {
		for (var i = 0; i < fMap.length / 2; i++) {
			if (fieldName == fMap[i + i + 1]) {
				var trueName = fMap[i + i];
				if (trueName != null && trueName.length > 0) {
					fieldName = trueName;
				}
				break;
			}
		}
	}
	var count = 0;
	while (!fieldMap[fieldName] && count++ <= 3) {
		fieldName = "$" + fieldName;
	}

	index += l1;
	var c2 = str.charAt (index++);
	if (c2 == 'A') {
		var field = fieldMap[fieldName];
		c2 = str.charAt(index++);
		var c3 = str.charCodeAt(index++);
		var l2 = c3 - baseChar;
		if (l2 < 0 && l2 != -2) {
			if (!fieldMap[fieldName]) {
				continue;
			}
			this[fieldName] = null;
		} else {
			if (l2 == -2) {
				var c4 = str.charCodeAt(index++);
				var l3 = c4 - baseChar;
				if (l3 < 0) return true;
				l2 = parseInt(str.substring(index, index + l3));
				if (l2 > 0x1000000) { // 16 * 1024 * 1024
					throw new RuntimeException("Array size reaches the limit of Java2Script Simple RPC!");
				}
				index += l3;
			}
			var arr = new Array (l2);
			var type = c2;
			for (var i = 0; i < l2; i++) {
				var s = null;
				var c4 = str.charCodeAt (index++);
				if (c2 != 'X') {
					var l3 = c4 - baseChar;
					if (l3 > 0) {
						s = str.substring (index, index + l3);
						index += l3;
					} else if (l3 == 0) {
						s = "";
					}
				} else {
					var c5 = str.charCodeAt (index++);
					var l3 = c5 - baseChar;
					if (l3 > 0) {
						s = str.substring (index, index + l3);
						index += l3;
					} else if (l3 == 0) {
						s = "";
					} else if (l3 == -2) {
						var c6 = str.charCodeAt (index++);
						var l4 = c6 - baseChar;
						if (l4 < 0) return true;
						var l5 = parseInt (str.substring( index, index + l4));
						if (l5 < 0) return true;
						index += l4;
						s = str.substring (index, index + l5);
						index += l5;
					}
					if (c4 == 117) { // 'u'
						s = Encoding.readUTF8(Encoding.decodeBase64(s));
					} else if (c4 == 85) { // 85 'U'
						s = Encoding.readUTF8(s);
					}
				}
				if (type == 'F' || type == 'D') {
					arr[i] = parseFloat (s);
				} else if (type == 'I' || type == 'L'
						|| type == 'S' || type == 'B') {
					arr[i] = parseInt (s);
				} else if (type == 'C') {
					arr[i] = String.fromCharCode (parseInt (s));
				} else if (type == 'b') {
					arr[i] = (s.charAt (0) == '1' || s.charAt (0) == 't');
				} else if (type == 'X') {
					arr[i] = s;
				}
			}
			if (!fieldMap[fieldName]) {
				continue;
			}
			this[fieldName] = arr;
		}
	} else {
		var c3 = str.charCodeAt (index++);
		var l2 = c3 - baseChar;
		var s = null;
		if (l2 > 0) {
			s = str.substring (index, index + l2);
			index += l2;
		} else if (l2 == 0) {
			s = "";
		} else if (l2 == -2) {
			var c4 = str.charCodeAt(index++);
			var l3 = c4 - baseChar;
			if (l3 < 0) return true;
			var l4 = parseInt(str.substring(index, index + l3));
			if (l4 < 0) return true;
			index += l3;
			s = str.substring(index, index + l4);
			index += l4;
		}
		if (!fieldMap[fieldName]) {
			continue;
		}
		var type = c2;
		if (type == 'F' || type == 'D') {
			this[fieldName] = parseFloat (s);
		} else if (type == 'I' || type == 'L'
				|| type == 'S' || type == 'B') {
			this[fieldName] = parseInt (s);
		} else if (type == 'C') {
			this[fieldName] = String.fromCharCode (parseInt (s));
		} else if (type == 'b') {
			this[fieldName] = (s.charAt (0) == '1' || s.charAt (0) == 't');
		} else if (type == 's') {
			this[fieldName] = s;
		} else if (type == 'u') {
			this[fieldName] = Encoding.readUTF8(Encoding.decodeBase64(s));
		} else if (type == 'U') {
			this[fieldName] = Encoding.readUTF8(s);
		}
	}
}
return true;
	 */
    public boolean deserialize(final String str) {
        return deserialize(str, 0);
    }

    @J2SIgnore
    public boolean deserialize(final String str, int start) {
        char baseChar = 'B';
        if (str == null || start < 0) return false;
        int length = str.length() - start;
        if (length <= 7 || !("WLL".equals(str.substring(start, start + 3)))) return false;
        int index = str.indexOf('#', start);
        if (index == -1) return false;
        index++;
        if (index >= length + start) return false;
        int size = 0;
        char nextChar = str.charAt(index);
        if (nextChar >= '0' && nextChar <= '9') {
            int last = index;
            index = str.indexOf('$', last);
            if (index == -1) return false;
            String sizeStr = str.substring(last + 1, index);
            sizeStr = sizeStr.replaceFirst("^0+", "");
            if (sizeStr.length() != 0) {
                try {
                    size = Integer.parseInt(sizeStr);
                } catch (NumberFormatException e) {
                    return false;
                }
            }
            if (size == 0) return true;
            index++;
            if (size > length + start - index) return false;
        }
        Map<String, Field> fieldMap = new HashMap<String, Field>();
        Set<Field> fieldSet = new HashSet<Field>();
        Class<?> clazz = this.getClass();
        while (clazz != null && !"net.sf.j2s.ajax.SimpleSerializable".equals(clazz.getName())) {
            Field[] fields = clazz.getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                fieldSet.add(fields[i]);
            }
            clazz = clazz.getSuperclass();
        }
        Field[] fields = fieldSet.toArray(new Field[0]);
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            int modifiers = field.getModifiers();
            if ((modifiers & (Modifier.PUBLIC | Modifier.PROTECTED)) != 0 && (modifiers & Modifier.TRANSIENT) == 0 && (modifiers & Modifier.STATIC) == 0) {
                String name = field.getName();
                fieldMap.put(name, field);
            }
        }
        int end = index + size;
        String[] fMap = fieldMapping();
        while (index < length + start && index < end) {
            char c1 = str.charAt(index++);
            int l1 = c1 - baseChar;
            if (l1 < 0) return true;
            String fieldName = str.substring(index, index + l1);
            if (fMap != null && fMap.length > 1) {
                for (int i = 0; i < fMap.length / 2; i++) {
                    if (fieldName.equals(fMap[i + i + 1])) {
                        String trueName = fMap[i + i];
                        if (trueName != null && trueName.length() > 0) {
                            fieldName = trueName;
                        }
                        break;
                    }
                }
            }
            while (fieldName.startsWith("$")) {
                if (fieldMap.containsKey(fieldName)) {
                    break;
                }
                fieldName = fieldName.substring(1);
            }
            index += l1;
            char c2 = str.charAt(index++);
            if (c2 == 'A') {
                Field field = (Field) fieldMap.get(fieldName);
                c2 = str.charAt(index++);
                char c3 = str.charAt(index++);
                int l2 = c3 - baseChar;
                try {
                    if (l2 < 0 && l2 != -2) {
                        if (field == null) {
                            continue;
                        }
                        field.set(this, null);
                    } else {
                        if (l2 == -2) {
                            char c4 = str.charAt(index++);
                            int l3 = c4 - baseChar;
                            if (l3 < 0) return true;
                            l2 = Integer.parseInt(str.substring(index, index + l3));
                            if (l2 > 0x1000000) {
                                throw new RuntimeException("Array size reaches the limit of Java2Script Simple RPC!");
                            }
                            index += l3;
                        }
                        if (c2 == '8') {
                            String byteStr = str.substring(index, index + l2);
                            index += l2;
                            if (field == null) {
                                continue;
                            }
                            byte[] bs = byteStr.getBytes("iso-8859-1");
                            field.set(this, bs);
                            continue;
                        }
                        String[] ss = new String[l2];
                        for (int i = 0; i < l2; i++) {
                            char c4 = str.charAt(index++);
                            if (c2 != 'X') {
                                int l3 = c4 - baseChar;
                                if (l3 > 0) {
                                    ss[i] = str.substring(index, index + l3);
                                    index += l3;
                                } else if (l3 == 0) {
                                    ss[i] = "";
                                }
                            } else {
                                char c5 = str.charAt(index++);
                                int l3 = c5 - baseChar;
                                if (l3 > 0) {
                                    ss[i] = str.substring(index, index + l3);
                                    index += l3;
                                } else if (l3 == 0) {
                                    ss[i] = "";
                                } else if (l3 == -2) {
                                    char c6 = str.charAt(index++);
                                    int l4 = c6 - baseChar;
                                    if (l4 < 0) return true;
                                    int l5 = Integer.parseInt(str.substring(index, index + l4));
                                    if (l5 < 0) return true;
                                    index += l4;
                                    ss[i] = str.substring(index, index + l5);
                                    index += l5;
                                }
                                if (c4 == 'u') {
                                    ss[i] = new String(Base64.base64ToByteArray(ss[i]), "utf-8");
                                } else if (c4 == 'U') {
                                    ss[i] = new String(ss[i].getBytes("iso-8859-1"), "utf-8");
                                }
                            }
                        }
                        if (field == null) {
                            continue;
                        }
                        switch(c2) {
                            case 'F':
                                {
                                    float[] fs = new float[l2];
                                    for (int i = 0; i < l2; i++) {
                                        if (ss[i] != null) {
                                            fs[i] = Float.parseFloat(ss[i]);
                                        }
                                    }
                                    field.set(this, fs);
                                    break;
                                }
                            case 'D':
                                {
                                    double[] ds = new double[l2];
                                    for (int i = 0; i < l2; i++) {
                                        if (ss[i] != null) {
                                            ds[i] = Double.parseDouble(ss[i]);
                                        }
                                    }
                                    field.set(this, ds);
                                    break;
                                }
                            case 'I':
                                {
                                    int[] ns = new int[l2];
                                    for (int i = 0; i < l2; i++) {
                                        if (ss[i] != null) {
                                            ns[i] = Integer.parseInt(ss[i]);
                                        }
                                    }
                                    field.set(this, ns);
                                    break;
                                }
                            case 'L':
                                {
                                    long[] ls = new long[l2];
                                    for (int i = 0; i < l2; i++) {
                                        if (ss[i] != null) {
                                            ls[i] = Long.parseLong(ss[i]);
                                        }
                                    }
                                    field.set(this, ls);
                                    break;
                                }
                            case 'S':
                                {
                                    short[] sts = new short[l2];
                                    for (int i = 0; i < l2; i++) {
                                        if (ss[i] != null) {
                                            sts[i] = Short.parseShort(ss[i]);
                                        }
                                    }
                                    field.set(this, sts);
                                    break;
                                }
                            case 'B':
                                {
                                    byte[] bs = new byte[l2];
                                    for (int i = 0; i < l2; i++) {
                                        if (ss[i] != null) {
                                            bs[i] = Byte.parseByte(ss[i]);
                                        }
                                    }
                                    field.set(this, bs);
                                    break;
                                }
                            case 'C':
                                {
                                    char[] cs = new char[l2];
                                    for (int i = 0; i < l2; i++) {
                                        if (ss[i] != null) {
                                            cs[i] = (char) Integer.parseInt(ss[i]);
                                        }
                                    }
                                    field.set(this, cs);
                                    break;
                                }
                            case 'b':
                                {
                                    boolean[] bs = new boolean[l2];
                                    for (int i = 0; i < l2; i++) {
                                        if (ss[i] != null && ss[i].length() > 0) {
                                            char c = ss[i].charAt(0);
                                            bs[i] = (c == '1' || c == 't');
                                        }
                                    }
                                    field.set(this, bs);
                                    break;
                                }
                            case 'X':
                                {
                                    field.set(this, ss);
                                    break;
                                }
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Parsing: " + str);
                    e.printStackTrace();
                }
            } else {
                char c3 = str.charAt(index++);
                int l2 = c3 - baseChar;
                String s = null;
                if (l2 > 0) {
                    s = str.substring(index, index + l2);
                    index += l2;
                } else if (l2 == 0) {
                    s = "";
                } else if (l2 == -2) {
                    char c4 = str.charAt(index++);
                    int l3 = c4 - baseChar;
                    if (l3 < 0) return true;
                    int l4 = Integer.parseInt(str.substring(index, index + l3));
                    if (l4 < 0) return true;
                    index += l3;
                    s = str.substring(index, index + l4);
                    index += l4;
                }
                Field field = (Field) fieldMap.get(fieldName);
                if (field == null) {
                    continue;
                }
                try {
                    switch(c2) {
                        case 'F':
                            field.setFloat(this, Float.parseFloat(s));
                            break;
                        case 'D':
                            field.setDouble(this, Double.parseDouble(s));
                            break;
                        case 'I':
                            field.setInt(this, Integer.parseInt(s));
                            break;
                        case 'L':
                            field.setLong(this, Long.parseLong(s));
                            break;
                        case 'S':
                            field.setShort(this, Short.parseShort(s));
                            break;
                        case 'B':
                            field.setByte(this, Byte.parseByte(s));
                            break;
                        case 'C':
                            field.setChar(this, (char) Integer.parseInt(s));
                            break;
                        case 'b':
                            field.setBoolean(this, s.charAt(0) == '1' || s.charAt(0) == 't');
                            break;
                        case 's':
                            field.set(this, s);
                            break;
                        case 'u':
                            s = new String(Base64.base64ToByteArray(s), "utf-8");
                            field.set(this, s);
                            break;
                        case 'U':
                            s = new String(s.getBytes("iso-8859-1"), "utf-8");
                            field.set(this, s);
                            break;
                    }
                } catch (Exception e) {
                    System.out.println("Parsing: " + s + "\r\n" + str);
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    /**
     * Fields with alias names.
     * @return
     */
    protected String[] fieldMapping() {
        return null;
    }

    /**
     * Fields to be ignored while differences are being calculated.
     * @return
     */
    protected String[] fieldDiffIgnored() {
        return null;
    }

    protected boolean bytesCompactMode() {
        return BYTES_COMPACT_MODE;
    }

    /**
	 * Override Object@clone, so this object can be cloned.
	 */
    @J2SIgnore
    public Object clone() throws CloneNotSupportedException {
        Object clone = super.clone();
        Set<Field> fieldSet = new HashSet<Field>();
        Class<?> clazz = this.getClass();
        while (clazz != null && !"net.sf.j2s.ajax.SimpleSerializable".equals(clazz.getName())) {
            Field[] fields = clazz.getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                fieldSet.add(fields[i]);
            }
            clazz = clazz.getSuperclass();
        }
        Field[] fields = fieldSet.toArray(new Field[0]);
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            int modifiers = field.getModifiers();
            if ((modifiers & (Modifier.PUBLIC | Modifier.PROTECTED)) != 0 && (modifiers & Modifier.TRANSIENT) == 0 && (modifiers & Modifier.STATIC) == 0) {
                Class<?> type = field.getType();
                Object value = null;
                try {
                    value = field.get(this);
                } catch (Exception e1) {
                }
                if (value != null && type.getName().startsWith("[")) {
                    if (type == float[].class) {
                        float[] as = (float[]) value;
                        float[] clones = new float[as.length];
                        for (int j = 0; j < clones.length; j++) {
                            clones[j] = as[j];
                        }
                        try {
                            field.set(clone, clones);
                        } catch (Exception e) {
                        }
                    } else if (type == double[].class) {
                        double[] as = (double[]) value;
                        double[] clones = new double[as.length];
                        for (int j = 0; j < clones.length; j++) {
                            clones[j] = as[j];
                        }
                        try {
                            field.set(clone, clones);
                        } catch (Exception e) {
                        }
                    } else if (type == int[].class) {
                        int[] as = (int[]) value;
                        int[] clones = new int[as.length];
                        for (int j = 0; j < clones.length; j++) {
                            clones[j] = as[j];
                        }
                        try {
                            field.set(clone, clones);
                        } catch (Exception e) {
                        }
                    } else if (type == long[].class) {
                        long[] as = (long[]) value;
                        long[] clones = new long[as.length];
                        for (int j = 0; j < clones.length; j++) {
                            clones[j] = as[j];
                        }
                        try {
                            field.set(clone, clones);
                        } catch (Exception e) {
                        }
                    } else if (type == short[].class) {
                        short[] as = (short[]) value;
                        short[] clones = new short[as.length];
                        for (int j = 0; j < clones.length; j++) {
                            clones[j] = as[j];
                        }
                        try {
                            field.set(clone, clones);
                        } catch (Exception e) {
                        }
                    } else if (type == byte[].class) {
                        byte[] as = (byte[]) value;
                        byte[] clones = new byte[as.length];
                        for (int j = 0; j < clones.length; j++) {
                            clones[j] = as[j];
                        }
                        try {
                            field.set(clone, clones);
                        } catch (Exception e) {
                        }
                    } else if (type == char[].class) {
                        char[] as = (char[]) value;
                        char[] clones = new char[as.length];
                        for (int j = 0; j < clones.length; j++) {
                            clones[j] = as[j];
                        }
                        try {
                            field.set(clone, clones);
                        } catch (Exception e) {
                        }
                    } else if (type == boolean[].class) {
                        boolean[] as = (boolean[]) value;
                        boolean[] clones = new boolean[as.length];
                        for (int j = 0; j < clones.length; j++) {
                            clones[j] = as[j];
                        }
                        try {
                            field.set(clone, clones);
                        } catch (Exception e) {
                        }
                    } else if (type == String[].class) {
                        String[] as = (String[]) value;
                        String[] clones = new String[as.length];
                        for (int j = 0; j < clones.length; j++) {
                            clones[j] = as[j];
                        }
                        try {
                            field.set(clone, clones);
                        } catch (Exception e) {
                        }
                    } else if (type == Object[].class) {
                        Object[] as = (Object[]) value;
                        Object[] clones = new Object[as.length];
                        for (int j = 0; j < clones.length; j++) {
                            clones[j] = as[j];
                        }
                        try {
                            field.set(clone, clones);
                        } catch (Exception e) {
                        }
                    }
                }
            }
        }
        return clone;
    }

    /**
	 * Get SimpleSerializable instance according to the given string. 
	 * 
	 * @param str String that is from POST data or GET query
	 * @return SimpleRPCRunnable instance. If request is bad request or 
	 * specified class name is invalid, null will be returned.
	 * 
	 * @j2sNative
var start = 0;
if (arguments.length == 2) {
	start = arguments[1];
}
if (str == null || start < 0) return null;
var length = str.length - start;
if (length <= 7 || str.substring(start, start + 3) != "WLL") return null;
var index = str.indexOf('#', start);
if (index == -1) return null;
var clazzName = str.substring(start + 6, index);
clazzName = clazzName.replace (/\$/g, '.');
var runnableClass = null;
if (Clazz.isClassDefined (clazzName)) {
	runnableClass = Clazz.evalType (clazzName);
}
if (runnableClass != null) {
	var obj = new runnableClass (Clazz.inheritArgs);
	if (obj != null && Clazz.instanceOf (obj,
			net.sf.j2s.ajax.SimpleSerializable)) {
		return obj;
	}
}
return net.sf.j2s.ajax.SimpleSerializable.UNKNOWN;
	 */
    public static SimpleSerializable parseInstance(String str) {
        return parseInstance(str, 0, null);
    }

    /**
	 * Get SimpleSerializable instance according to the given string, 
	 * starting from the given index. 
	 * 
	 * @param str
	 * @param start
	 * @return
	 */
    @J2SIgnore
    public static SimpleSerializable parseInstance(String str, int start) {
        return parseInstance(str, start, null);
    }

    /**
	 * Get SimpleSerializable instance according to the given string and the filter. 
	 * 
	 * @param str String that is from POST data or GET query  
	 * @param filter SimpleFilter is used to filter out those invalid class name
	 * @return SimpleRPCRunnable instance. If request is bad request or 
	 * specified class name is invalid, null will be returned.
	 */
    @J2SIgnore
    public static SimpleSerializable parseInstance(String str, SimpleFilter filter) {
        return parseInstance(str, 0, filter);
    }

    /**
	 * Get SimpleSerializable instance according to the given string starting from
	 * the given index and the filter. 
	 * 
	 * @param str
	 * @param filter
	 * @return
	 */
    @J2SIgnore
    public static SimpleSerializable parseInstance(String str, int start, SimpleFilter filter) {
        if (str == null || start < 0) return null;
        int length = str.length() - start;
        if (length <= 7 || !("WLL".equals(str.substring(start, start + 3)))) return null;
        int index = str.indexOf('#', start);
        if (index == -1) return null;
        String clazzName = str.substring(start + 6, index);
        if (filter != null) {
            if (!filter.accept(clazzName)) return null;
        }
        try {
            Class<?> runnableClass = Class.forName(clazzName);
            if (runnableClass != null) {
                Constructor<?> constructor = runnableClass.getConstructor(new Class[0]);
                Object obj = constructor.newInstance(new Object[0]);
                if (obj != null && obj instanceof SimpleSerializable) {
                    return (SimpleSerializable) obj;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return UNKNOWN;
    }
}
