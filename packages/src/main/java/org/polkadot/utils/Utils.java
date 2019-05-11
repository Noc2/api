package org.polkadot.utils;

import com.google.common.collect.Lists;
import com.google.common.primitives.UnsignedBytes;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.polkadot.types.codec.CodecUtils;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Utils {

    /**
     * @name isHex
     * @summary Tests for a hex string.
     * @description Checks to see if the input value is a `0x` prefixed hex string. Optionally (`bitLength` !== -1) checks to see if the bitLength is correct.
     * @example <BR>
     * <p>
     * ```javascript
     * import { isHex } from '@polkadot/util';
     * <p>
     * isHex('0x1234'); // => true
     * isHex('0x1234', 8); // => false
     * ```
     */
    static final String HEX_REGEX = "^0x[a-fA-F0-9]+$";

    //export default function isHex (value: any, bitLength: number = -1, ignoreLength: boolean = false): value is string | String {
    public static boolean isHex(Object value) {
        return isHex(value, -1, false);
    }

    public static boolean isHex(Object _value, int bitLength, boolean ignoreLength) {
        CharSequence value = _value.toString();
        boolean isValidHex = value.equals("0x") || (value instanceof String && Pattern.matches(HEX_REGEX, value));

        if (isValidHex && bitLength != -1) {
            return value.length() == (2 + (int) Math.ceil(bitLength / 4));
        }

        return isValidHex && (ignoreLength || (value.length() % 2 == 0));
    }


    /**
     * @name hexToU8a
     * @summary Creates a Buffer object from a hex string.
     * @description `null` inputs returns an empty `Uint8Array` result. Hex input values return the actual bytes value converted to a Uint8Array. Anything that is not a hex string (including the `0x` prefix) throws an error.
     * @example <BR>
     * <p>
     * ```javascript
     * import { hexToU8a } from '@polkadot/util';
     * <p>
     * hexToU8a('0x80001f'); // Uint8Array([0x80, 0x00, 0x1f])
     * hexToU8a('0x80001f', 32); // Uint8Array([0x00, 0x80, 0x00, 0x1f])
     * ```
     */
    //export default function hexToU8a (_value?: string | null, bitLength: number = -1): Uint8Array {
    public static byte[] hexToU8a(String value, int bitLength) {
        if (value == null) {
            return new byte[0];
        }

        assert isHex(value) : "Expected hex value to convert, found " + value;

        value = hexStripPrefix(value);
        int valLength = value.length() / 2;
        int bufLength = (int) Math.ceil((
                bitLength == -1
                        ? valLength
                        : bitLength / 8f));

        byte[] result = new byte[bufLength];
        int offSet = Math.max(0, bufLength - valLength);

        for (int index = 0; index < bufLength; index++) {
            String byteStr = value.substring(index * 2, index * 2 + 2);
            result[index + offSet] = UnsignedBytes.parseUnsignedByte(byteStr, 16);
        }
        return result;
    }

    public static byte[] hexToU8a(String value) {
        return hexToU8a(value, -1);
    }


    /**
     * @name hexStripPrefix
     * @summary Strips any leading `0x` prefix.
     * @description Tests for the existence of a `0x` prefix, and returns the value without the prefix. Un-prefixed values are returned as-is.
     * @example <BR>
     * <p>
     * ```javascript
     * import { hexStripPrefix } from '@polkadot/util';
     * <p>
     * console.log('stripped', hexStripPrefix('0x1234')); // => 1234
     * ```
     */
    //export default function hexStripPrefix (value?: string | null): string {
    static String UNPREFIX_HEX_REGEX = "^[a-fA-F0-9]+$";

    public static String hexStripPrefix(String value) {
        if (value == null) {
            return "";
        }

        if (hexHasPrefix(value)) {
            return value.substring(2);
        }

        if (Pattern.matches(UNPREFIX_HEX_REGEX, value)) {
            return value;
        }

        throw new RuntimeException("Invalid hex " + value + " passed to hexStripPrefix");
    }

    /**
     * @name hexHasPrefix
     * @summary Tests for the existence of a `0x` prefix.
     * @description Checks for a valid hex input value and if the start matched `0x`
     * @example <BR>
     * <p>
     * ```javascript
     * import { hexHasPrefix } from '@polkadot/util';
     * <p>
     * console.log('has prefix', hexHasPrefix('0x1234')); // => true
     * ```
     */
    //export default function hexHasPrefix (value?: string | null): boolean {
    //    return !!(value && isHex(value, -1, true) && value.substr(0, 2) === '0x');
    //}
    public static boolean hexHasPrefix(String value) {
        if (value != null
                && isHex(value, -1, true)
                && value.substring(0, 2).equals("0x")) {
            return true;
        }
        return false;
    }


    /**
     * @name isU8a
     * @summary Tests for a `Uint8Array` object instance.
     * @description Checks to see if the input object is an instance of `Uint8Array`.
     * @example <BR>
     * <p>
     * ```javascript
     * import { isUint8Array } from '@polkadot/util';
     * <p>
     * console.log('isU8a', isU8a([])); // => false
     * ```
     */
    //export default function isU8a (value?: any): value is Uint8Array {
    public static boolean isU8a(Object value) {
        return value instanceof byte[];
    }


    /**
     * @param _value              The value to convert
     * @param _options            Options to pass while converting
     * @param _options.isLe       Convert using Little Endian
     * @param _options.isNegative Convert using two's complement
     * @name hexToBn
     * @summary Creates a BN.js bignumber object from a hex string.
     * @description `null` inputs returns a `BN(0)` result. Hex input values return the actual value converted to a BN. Anything that is not a hex string (including the `0x` prefix) throws an error.
     * @example <BR>
     * <p>
     * ```javascript
     * import { hexToBn } from '@polkadot/util';
     * <p>
     * hexToBn('0x123480001f'); // => BN(0x123480001f)
     * ```
     */
    //export default function hexToBn (value?: string | number | null, options: ToBnOptions | boolean = { isLe: false, isNegative: false }): BN {
    public static BigInteger hexToBn(Object value, boolean isLe, boolean isNegative) {
        if (value == null) {
            return BigInteger.ZERO;
        }

        String rawValue = hexStripPrefix((String) value);

        if (isLe) {
            //"12345678" --- "78563412"
            StringBuilder reverse = new StringBuilder(rawValue).reverse();
            for (int i = 0; i < reverse.length(); i += 2) {
                char c1 = reverse.charAt(i);
                char c2 = reverse.charAt(i + 1);

                reverse.setCharAt(i + 1, c1);
                reverse.setCharAt(i, c2);
            }
            rawValue = reverse.toString();
        }

        BigInteger bigInteger = new BigInteger(rawValue, 16);

        if (isNegative) {
            //TODO 2019-05-08 23:04
            throw new UnsupportedOperationException();
        }
        return bigInteger;

        // FIXME: Use BN's 3rd argument `isLe` once this issue is fixed
        // https://github.com/indutny/bn.js/issues/208
        //const bn = new BN((_options.isLe ? reverse(_value) : _value) || '00', 16);

        // fromTwos takes as parameter the number of bits, which is the hex length
        // multiplied by 4.
        //return _options.isNegative ? bn.fromTwos(_value.length * 4) : bn;
    }

    /**
     * @param value              The value to convert
     * @param options            Options to pass while converting
     * @param options.isLe       Convert using Little Endian
     * @param options.isNegative Convert using two's complement
     * @name u8aToBn
     * @summary Creates a BN from a Uint8Array object.
     * @description `UInt8Array` input values return the actual BN. `null` or `undefined` values returns an `0x0` value.
     * @example <BR>
     * <p>
     * ```javascript
     * import { u8aToBn } from '@polkadot/util';
     * <p>
     * u8aToHex(new Uint8Array([0x68, 0x65, 0x6c, 0x6c, 0xf])); // 0x68656c0f
     * ```
     */
    //export default function u8aToBn (value: Uint8Array, options: ToBnOptions | boolean = { isLe: true, isNegative: false }):
    public static BigInteger u8aToBn(byte[] value, boolean isLe, boolean isNegative) {
        return hexToBn(
                u8aToHex(value),
                isLe, isNegative
        );
    }


    /**
     * @name bnToBn
     * @summary Creates a BN value from a BN.js bignumber or number input.
     * @description `null` inputs returns a `0x0` result, BN values returns the value, numnbers returns a BN representation.
     * @example <BR>
     * <p>
     * ```javascript
     * import BN from 'bn.js';
     * import { bnToBn } from '@polkadot/util';
     * <p>
     * bnToBn(0x1234); // => BN(0x1234)
     * bnToBn(new BN(0x1234)); // => BN(0x1234)
     * ```
     */
    //export default function bnToBn (value?: BN | number | null): BN {
    public static BigInteger bnToBn(Object value) {
        if (value == null) {
            return BigInteger.ZERO;
        }

        if (value instanceof BigInteger) {
            return (BigInteger) value;
        } else if (value instanceof Number) {
            return new BigInteger(value.toString());
        } else if (value instanceof String) {
            return new BigInteger((String) value, 16);
        }

        throw new RuntimeException(" bnToBn " + value);
    }

    final static String ZERO_STR = "0x00";


    /**
     * @name bnToHex
     * @summary Creates a hex value from a BN.js bignumber object.
     * @description `null` inputs returns a `0x` result, BN values return the actual value as a `0x` prefixed hex value. Anything that is not a BN object throws an error. With `bitLength` set, it fixes the number to the specified length.
     * @example <BR>
     * <p>
     * ```javascript
     * import BN from 'bn.js';
     * import { bnToHex } from '@polkadot/util';
     * <p>
     * bnToHex(new BN(0x123456)); // => '0x123456'
     * ```
     */
    //export default function bnToHex (value?: BN | number | null, options: number | Options = { bitLength: -1, isLe: false, isNegative: false }): string {
    public static String bnToHex(BigInteger value, boolean isLe, boolean isNegtive, int bitLength) {
        /*
        *
  if (!value) {
    return ZERO_STR;
  }

  const _options = {
    isLe: false,
    isNegative: false,
    // Backwards-compatibility
    ...(isNumber(options) ? { bitLength: options } : options)
  };

  return u8aToHex(bnToU8a(value, _options));
        * */
        if (value == null) {
            return ZERO_STR;
        }

        return u8aToHex(bnToU8a(value, isLe, isNegtive, bitLength));
    }


    /**
     * @name bnToU8a
     * @summary Creates a Uint8Array object from a BN.
     * @description `null`/`undefined`/`NaN` inputs returns an empty `Uint8Array` result. `BN` input values return the actual bytes value converted to a `Uint8Array`. Optionally convert using little-endian format if `isLE` is set.
     * @example <BR>
     * <p>
     * ```javascript
     * import { bnToU8a } from '@polkadot/util';
     * <p>
     * bnToU8a(new BN(0x1234)); // => [0x12, 0x34]
     * ```
     */
    //export default function bnToU8a (value: BN | number | null, options?: Options): Uint8Array;
    //export default function bnToU8a (value: BN | number | null, bitLength?: number, isLe?: boolean): Uint8Array;
    //export default function bnToU8a (value: BN | number | null, arg1: number | Options = { bitLength: -1, isLe: true, isNegative: false },arg2?: boolean): Uint8Array {
    public static byte[] bnToU8a(BigInteger value, boolean isLe, boolean isNegtive, int bitLength) {

        /*
        * const _options: Options = {
    isLe: true,
    isNegative: false,
    bitLength: -1,
    ...isNumber(arg1) ? { bitLength: arg1, isLe: arg2 } : arg1
  };

  const valueBn = bnToBn(value);
  let byteLength = _options.bitLength === -1
    ? Math.ceil(valueBn.bitLength() / 8)
    : Math.ceil(_options.bitLength! / 8);

  if (!value) {
    return _options.bitLength === -1
      ? new Uint8Array([])
      : new Uint8Array(byteLength);
  }

  const output = new Uint8Array(byteLength);
  const bn = _options.isNegative ? valueBn.toTwos(byteLength * 8) : valueBn;

  output.set(
    bn.toArray(_options.isLe ? 'le' : 'be', byteLength),
    0
  );

  return output;*/

        BigInteger valueBn = bnToBn(value);
        int byteLength;
        if (bitLength == -1) {
            byteLength = (int) Math.ceil(valueBn.toByteArray().length / 8f);
        } else {
            byteLength = (int) Math.ceil(bitLength / 8f);
        }

        if (value == null) {
            if (bitLength == -1) {
                return new byte[0];
            } else {
                return new byte[byteLength];
            }
        }

        byte[] output = new byte[byteLength];

        if (isNegtive) {
            //TODO  valueBn.negate()
            //const bn = _options.isNegative ? valueBn.toTwos(byteLength * 8) : valueBn;
        }

        //big-endian
        byte[] bytes = valueBn.toByteArray();
        if (isLe) {
            bytes = toByteArrayLittleEndianUnsigned(valueBn);
        }
        if (output.length != bytes.length) {
            throw new RuntimeException();
        }

        return bytes;

    }

    public static void main(String[] argv) {
        BigInteger bi = new BigInteger("1234");
        System.out.println(java.util.Arrays.toString((bi.toByteArray())));

        System.out.println(java.util.Arrays.toString(toByteArrayLittleEndianUnsigned(bi)));
    }

    public static byte[] toByteArrayLittleEndianUnsigned(BigInteger bi) {
        byte[] extractedBytes = toByteArrayUnsigned(bi);
        ArrayUtils.reverse(extractedBytes);
        //byte[] reversed = ByteUtils.reverseArray(extractedBytes);
        return extractedBytes;
    }

    public static byte[] toByteArrayUnsigned(BigInteger bi) {
        byte[] extractedBytes = bi.toByteArray();
        int skipped = 0;
        boolean skip = true;
        for (byte b : extractedBytes) {
            boolean signByte = b == (byte) 0x00;
            if (skip && signByte) {
                skipped++;
                continue;
            } else if (skip) {
                skip = false;
            }
        }
        extractedBytes = Arrays.copyOfRange(extractedBytes, skipped,
                extractedBytes.length);
        return extractedBytes;
    }


    /**
     * @name compactFromU8a
     * @description Retrievs the offset and encoded length from a compact-prefixed value
     * @example <BR>
     * <p>
     * ```javascript
     * import { compactFromU8a } from '@polkadot/util';
     * <p>
     * const [offset, length] = compactFromU8a(new Uint8Array([254, 255, 3, 0]), 32));
     * <p>
     * console.log('value offset=', offset, 'length=', length); // 4, 0xffff
     * ```
     */
    //export default function compactFromU8a (_input: Uint8Array | string, bitLength: BitLength = DEFAULT_BITLENGTH): [number, BN] {
    public static Pair<Integer, BigInteger> compactFromU8a(Object _input, int bitLength) {
          /*
        *   const input = u8aToU8a(_input);
  const flag = input[0] & 0b11;

  if (flag === 0b00) {
    return [1, new BN(input[0]).shrn(2)];
  } else if (flag === 0b01) {
    return [2, u8aToBn(input.slice(0, 2), true).shrn(2)];
  } else if (flag === 0b10) {
    return [4, u8aToBn(input.slice(0, 4), true).shrn(2)];
  }

  const length = new BN(input[0])
    .shrn(2) // clear flag
    .addn(4) // add 4 for base length
    .toNumber();
  const offset = 1 + length;

  return [offset, u8aToBn(input.subarray(1, offset), true)];
        * */
        byte[] input = u8aToU8a(_input);
        int flag = UnsignedBytes.toInt(input[0]) & 0b11;

        if (flag == 0b00) {
            //shift right
            return Pair.of(1, new BigInteger(UnsignedBytes.toInt(input[0]) + "").shiftRight(2));
        } else if (flag == 0b01) {
            byte[] subarray = ArrayUtils.subarray(input, 0, 2);
            return Pair.of(2, u8aToBn(subarray, true, false).shiftRight(2));
        } else if (flag == 0b10) {
            byte[] subarray = ArrayUtils.subarray(input, 0, 4);
            return Pair.of(4, u8aToBn(subarray, true, false).shiftRight(2));
        }


        int length = new BigInteger(UnsignedBytes.toInt(input[0]) + "")
                .shiftRight(2)
                .add(new BigInteger("4"))
                .intValue();

        int offset = length + 1;
        return Pair.of(offset, u8aToBn(ArrayUtils.subarray(input, 1, offset), true, false));
    }

    public static Pair<Integer, BigInteger> compactFromU8a(Object input) {
        return compactFromU8a(input, 32);
    }

    /**
     * @name u8aToString
     * @summary Creates a utf-8 string from a Uint8Array object.
     * @description `UInt8Array` input values return the actual decoded utf-8 string. `null` or `undefined` values returns an empty string.
     * @example <BR>
     * <p>
     * ```javascript
     * import { u8aToString } from '@polkadot/util';
     * <p>
     * u8aToString(new Uint8Array([0x68, 0x65, 0x6c, 0x6c, 0x6f])); // hello
     * ```
     */
    //export default function u8aToString (value?: Uint8Array | null): string {
    public static String u8aToString(byte[] value) {
        if (value == null || value.length == 0) {
            return "";
        }
//  return decoder.decode(value);
        return new String(value);
    }


    static final String ALPHABET = "0123456789abcdef";

    /**
     * @name u8aToHex
     * @summary Creates a hex string from a Uint8Array object.
     * @description `UInt8Array` input values return the actual hex string. `null` or `undefined` values returns an `0x` string.
     * @example <BR>
     * <p>
     * ```javascript
     * import { u8aToHex } from '@polkadot/util';
     * <p>
     * u8aToHex(new Uint8Array([0x68, 0x65, 0x6c, 0x6c, 0xf])); // 0x68656c0f
     * ```
     */
    //export default function u8aToHex (value?: Uint8Array | null, bitLength: number = -1, isPrefixed: boolean = true): string {
    public static String u8aToHex(byte[] value, int bitLength, boolean isPrefixed) {
        String prefix = isPrefixed ? "0x" : "";

        if (ArrayUtils.isEmpty(value)) {
            return prefix;
        }

        int byteLength = (int) Math.ceil(bitLength / 8f);

        if (byteLength > 0 && value.length > byteLength) {
            int halfLength = (int) Math.ceil(byteLength / 2f);

            String left = u8aToHex(ArrayUtils.subarray(value, 0, halfLength), -1, isPrefixed);
            String right = u8aToHex(ArrayUtils.subarray(value, value.length - halfLength, value.length), -1, false);

            return left + "…" + right;
        }
        // based on comments in https://stackoverflow.com/questions/40031688/javascript-arraybuffer-to-hex and
        // implementation in http://jsben.ch/Vjx2V - optimisation here suggests that a forEach loop is faster
        // than reduce as well (clocking at in 90% of the reduce speed with tweaking in the playpen above)
        //return value.reduce((result, value) => {
        //    return result + ALPHABET[value >> 4] + ALPHABET[value & 15];
        //}, prefix);
        StringBuilder stringBuilder = new StringBuilder(prefix);

        for (byte b : value) {
            int ub = UnsignedBytes.toInt(b);
            stringBuilder.append(ALPHABET.charAt(ub >> 4)).append(ALPHABET.charAt(ub & 15));
        }
        return stringBuilder.toString();
    }

    public static String u8aToHex(byte[] value) {
        return u8aToHex(value, -1, true);
    }


    /**
     * @name stringToU8a
     * @summary Creates a Uint8Array object from a utf-8 string.
     * @description String input values return the actual encoded `UInt8Array`. `null` or `undefined` values returns an empty encoded array.
     * @example <BR>
     * <p>
     * ```javascript
     * import { stringToU8a } from '@polkadot/util';
     * <p>
     * stringToU8a('hello'); // [0x68, 0x65, 0x6c, 0x6c, 0x6f]
     * ```
     */
    //export default function stringToU8a (value?: string): Uint8Array {
    public static byte[] stringToU8a(String value) {
        if (StringUtils.isEmpty(value)) {
            return new byte[0];
        }

        //TODO 2019-05-09 00:48 test
        return value.getBytes();
    }


    /**
     * @name compactAddLength
     * @description Adds a length prefix to the input value
     * @example <BR>
     * <p>
     * ```javascript
     * import { compactAddLength } from '@polkadot/util';
     * <p>
     * console.log(compactAddLength(new Uint8Array([0xde, 0xad, 0xbe, 0xef]))); // Uint8Array([4 << 2, 0xde, 0xad, 0xbe, 0xef])
     * ```
     */
    //export default function compactAddLength (input: Uint8Array): Uint8Array {
    public static byte[] compactAddLength(byte[] input) {
        return u8aConcat(Lists.newArrayList(
                compactToU8a(input.length),
                input)
        );
    }


    final static BigInteger MAX_U8 = BigInteger.valueOf(2).pow(8 - 2).subtract(BigInteger.ONE);
    final static BigInteger MAX_U16 = BigInteger.valueOf(2).pow(16 - 2).subtract(BigInteger.ONE);
    final static BigInteger MAX_U32 = BigInteger.valueOf(2).pow(32 - 2).subtract(BigInteger.ONE);
//const MAX_U8 = new BN(2).pow(new BN(8 - 2)).subn(1);
//const MAX_U16 = new BN(2).pow(new BN(16 - 2)).subn(1);
//const MAX_U32 = new BN(2).pow(new BN(32 - 2)).subn(1);

    /**
     * @name compactToU8a
     * @description Encodes a number into a compact representation
     * @example <BR>
     * <p>
     * ```javascript
     * import { compactToU8a } from '@polkadot/util';
     * <p>
     * console.log(compactToU8a(511, 32)); // Uint8Array([0b11111101, 0b00000111])
     * ```
     */
    //export default function compactToU8a (_value: BN | number): Uint8Array {
    public static byte[] compactToU8a(Object _value) {
        BigInteger value = bnToBn(_value);

        if (value.compareTo(MAX_U8) <= 0) {
            return new byte[]{UnsignedBytes.parseUnsignedByte((value.intValue() << 2) + "")};
        } else if (value.compareTo(MAX_U16) <= 0) {
            return bnToU8a(value.shiftLeft(2).add(BigInteger.valueOf(0b01)), true, false, 16);
        } else if (value.compareTo(MAX_U32) <= 0) {
            return bnToU8a(value.shiftLeft(2).add(BigInteger.valueOf(0b10)), true, false, 32);
        }

        byte[] u8a = bnToU8a(value, true, false, -1);
        int length = u8a.length;

        while (u8a[length - 1] == 0) {
            length--;
        }

        assert length >= 4 : "Previous tests match anyting less than 2^30; qed";

        return u8aConcat(Lists.newArrayList(
                // substract 4 as minimum (also catered for in decoding)
                new byte[]{UnsignedBytes.parseUnsignedByte((((length - 4) << 2) + 0b11) + "")},
                ArrayUtils.subarray(u8a, 0, length)
        ));
    }

    /**
     * @name u8aConcat
     * @summary Creates a concatenated Uint8Array from the inputs.
     * @description Concatenates the input arrays into a single `UInt8Array`.
     * @example <BR>
     * <p>
     * ```javascript
     * import { u8aConcat } from '@polkadot/util';
     * <p>
     * u8aConcat(
     * new Uint8Array([1, 2, 3]),
     * new Uint8Array([4, 5, 6])
     * ); // [1, 2, 3, 4, 5, 6]
     * ```
     */
    //export default function u8aConcat (..._list: Array<Uint8Array | string>): Uint8Array {
    public static byte[] u8aConcat(List<byte[]> _list) {
        List<byte[]> list = _list.stream().map(e -> u8aToU8a(e)).collect(Collectors.toList());

        int length = list.stream().mapToInt(e -> e.length).sum();
        byte[] result = new byte[length];
        int offset = 0;

        for (byte[] bytes : list) {
            System.arraycopy(bytes, 0, result, offset, bytes.length);
            offset += bytes.length;
        }
        return result;
    }


    /**
     * @name u8aToU8a
     * @summary Creates a Uint8Array value from a Uint8Array, Buffer, string or hex input.
     * @description `null` ior `undefined` nputs returns a `[]` result, Uint8Array values returns the value, hex strings returns a Uint8Array representation.
     * @example <BR>
     * <p>
     * ```javascript
     * import { u8aToU8a } from '@polkadot/util';
     * <p>
     * u8aToU8a(new Uint8Array([0x12, 0x34]); // => Uint8Array([0x12, 0x34])
     * u8aToU8a(0x1234); // => Uint8Array([0x12, 0x34])
     * ```
     */
    //export default function u8aToU8a (value?: Array<number> | Buffer | Uint8Array | string | null): Uint8Array {
    public static byte[] u8aToU8a(Object value) {
        if (value == null) {
            return new byte[0];
        }

        //if (isBuffer(value)) {
        //    return bufferToU8a(value);
        //}

        if (value instanceof String) {
            String strValue = (String) value;
            return isHex(strValue)
                    ? hexToU8a(strValue)
                    : stringToU8a(strValue);
        }

        if (value instanceof byte[]) {
            return (byte[]) value;
        }

        if (value.getClass().isArray()) {
            List<Object> objects = CodecUtils.arrayLikeToList(value);
            byte[] result = new byte[objects.size()];
            for (int i = 0; i < objects.size(); i++) {
                Number number = (Number) objects.get(i);
                result[i] = UnsignedBytes.parseUnsignedByte(number.toString());
            }
            return result;
        }

        return (byte[]) value;
    }


    /**
     * @name xxhashAsU8a
     * @summary Creates a xxhash64 u8a from the input.
     * @description From either a `string`, `Uint8Array` or a `Buffer` input, create the xxhash64 and return the result as a `Uint8Array` with the specified `bitLength`.
     * @example <BR>
     * <p>
     * ```javascript
     * import { xxhashAsU8a } from '@polkadot/util-crypto';
     * <p>
     * xxhashAsU8a('abc'); // => 0x44bc2cf5ad770999
     * ```
     */
    //  export default function xxhashAsU8a (data: Buffer | Uint8Array | string, bitLength: number = 64): Uint8Array {
    //const iterations = Math.ceil(bitLength / 64);
    //
    //      if (isReady()) {
    //          return twox(u8aToU8a(data), iterations);
    //      }
    //
    //const u8a = new Uint8Array(Math.ceil(bitLength / 8));
    //
    //      for (let seed = 0; seed < iterations; seed++) {
    //          u8a.set(xxhash64AsBn(data, seed).toArray('le', 8), seed * 8);
    //      }
    //
    //      return u8a;
    //  }
    public static byte[] xxhashAsU8a(Object data, int bitLength) {
        if (bitLength <= 0) {
            bitLength = 64;
        }
        //
        //int iterations = (int) Math.ceil(bitLength / 64f);
        //
        //XXHashFactory factory = XXHashFactory.fastestInstance();
        //
        ////byte[] data = "12345345234572".getBytes("UTF-8");
        //ByteArrayInputStream in = new ByteArrayInputStream((byte[]) data);
        //
        //int seed = 0x9747b28c; // used to initialize the hash value, use whatever
        //// value you want, but always the same
        //StreamingXXHash32 hash32 = factory.newStreamingHash32(seed);
        //byte[] buf = new byte[8]; // for real-world usage, use a larger buffer, like 8192 bytes
        //for (; ; ) {
        //    int read = in.read(buf);
        //    if (read == -1) {
        //        break;
        //    }
        //    hash32.update(buf, 0, read);
        //}
        //int hash = hash32.getValue();
        //TODO 2019-05-11 02:56
        new UnsupportedOperationException().printStackTrace();
        //TODO 2019-05-11 02:56
        return (byte[]) data;
    }


    /**
     * @name stringLowerFirst
     * @summary Lowercase the first letter of a string
     * @description Lowercase the first letter of a string
     * @example <BR>
     * <p>
     * ```javascript
     * import { stringLowerFirst } from '@polkadot/util';
     * <p>
     * stringLowerFirst('ABC'); // => 'aBC'
     * ```
     */
    //export default function stringLowerFirst (value?: string | null): string {
    public static String stringLowerFirst(String value) {
        if (StringUtils.isEmpty(value)) {
            return "";
        }

        return value.substring(0, 1).toLowerCase() + value.substring(1);
    }

    public static boolean isContainer(Object object) {
        if (object instanceof Collection
                || object instanceof Map) {
            return true;
        }
        if (object.getClass().isArray()) {
            Class<?> componentType = object.getClass().getComponentType();
            if (componentType.isPrimitive()) {
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    //public static String substr(String str, int startIndex, int length) {
    //    return str.substring()
    //}
}