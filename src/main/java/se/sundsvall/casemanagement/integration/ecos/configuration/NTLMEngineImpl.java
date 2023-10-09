package se.sundsvall.casemanagement.integration.ecos.configuration;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.hc.client5.http.impl.auth.NTLMEngine;
import org.apache.hc.client5.http.impl.auth.NTLMEngineException;
import org.apache.hc.client5.http.utils.ByteArrayBuilder;

import lombok.Getter;

/**
 * This was a final package private class from external package. Therefore, we had no option but copying the
 * impl-class...
 * Not the most pretty solution and something we add to the list of future improvements.
 */
final class NTLMEngineImpl implements NTLMEngine {
	static final int FLAG_REQUEST_UNICODE_ENCODING = 1;
	static final int FLAG_REQUEST_OEM_ENCODING = 2;
	static final int FLAG_REQUEST_TARGET = 4;
	static final int FLAG_REQUEST_SIGN = 16;
	static final int FLAG_REQUEST_SEAL = 32;
	static final int FLAG_REQUEST_LAN_MANAGER_KEY = 128;
	static final int FLAG_REQUEST_NTLMv1 = 512;
	static final int FLAG_DOMAIN_PRESENT = 4096;
	static final int FLAG_WORKSTATION_PRESENT = 8192;
	static final int FLAG_REQUEST_ALWAYS_SIGN = 32768;
	static final int FLAG_REQUEST_NTLM2_SESSION = 524288;
	static final int FLAG_REQUEST_VERSION = 33554432;
	static final int FLAG_TARGETINFO_PRESENT = 8388608;
	static final int FLAG_REQUEST_128BIT_KEY_EXCH = 536870912;
	static final int FLAG_REQUEST_EXPLICIT_KEY_EXCH = 1073741824;
	static final int FLAG_REQUEST_56BIT_ENCRYPTION = Integer.MIN_VALUE;
	static final int MSV_AV_EOL = 0;
	static final int MSV_AV_NB_COMPUTER_NAME = 1;
	static final int MSV_AV_NB_DOMAIN_NAME = 2;
	static final int MSV_AV_DNS_COMPUTER_NAME = 3;
	static final int MSV_AV_DNS_DOMAIN_NAME = 4;
	static final int MSV_AV_DNS_TREE_NAME = 5;
	static final int MSV_AV_FLAGS = 6;
	static final int MSV_AV_TIMESTAMP = 7;
	static final int MSV_AV_SINGLE_HOST = 8;
	static final int MSV_AV_TARGET_NAME = 9;
	static final int MSV_AV_CHANNEL_BINDINGS = 10;
	static final int MSV_AV_FLAGS_ACCOUNT_AUTH_CONSTAINED = 1;
	static final int MSV_AV_FLAGS_MIC = 2;
	static final int MSV_AV_FLAGS_UNTRUSTED_TARGET_SPN = 4;
	private static final Charset UNICODE_LITTLE_UNMARKED = Charset.forName("UnicodeLittleUnmarked");
	private static final Charset DEFAULT_CHARSET;
	private static final SecureRandom RND_GEN;
	private static final byte[] SIGNATURE;
	private static final byte[] SIGN_MAGIC_SERVER;
	private static final byte[] SIGN_MAGIC_CLIENT;
	private static final byte[] SEAL_MAGIC_SERVER;
	private static final byte[] SEAL_MAGIC_CLIENT;
	private static final byte[] MAGIC_TLS_SERVER_ENDPOINT;
	private static final String TYPE_1_MESSAGE;

	static {
		DEFAULT_CHARSET = StandardCharsets.US_ASCII;
		SecureRandom rnd = null;

		try {
			rnd = SecureRandom.getInstance("SHA1PRNG");
		} catch (final Exception var2) {}

		RND_GEN = rnd;
		SIGNATURE = getNullTerminatedAsciiString("NTLMSSP");
		SIGN_MAGIC_SERVER = getNullTerminatedAsciiString("session key to server-to-client signing key magic constant");
		SIGN_MAGIC_CLIENT = getNullTerminatedAsciiString("session key to client-to-server signing key magic constant");
		SEAL_MAGIC_SERVER = getNullTerminatedAsciiString("session key to server-to-client sealing key magic constant");
		SEAL_MAGIC_CLIENT = getNullTerminatedAsciiString("session key to client-to-server sealing key magic constant");
		MAGIC_TLS_SERVER_ENDPOINT = "tls-server-end-point:".getBytes(StandardCharsets.US_ASCII);
		TYPE_1_MESSAGE = (new NTLMEngineImpl.Type1Message()).getResponse();
	}

	NTLMEngineImpl() {}

	private static byte[] getNullTerminatedAsciiString(String source) {
		final byte[] bytesWithoutNull = source.getBytes(StandardCharsets.US_ASCII);
		final byte[] target = new byte[bytesWithoutNull.length + 1];
		System.arraycopy(bytesWithoutNull, 0, target, 0, bytesWithoutNull.length);
		target[bytesWithoutNull.length] = 0;
		return target;
	}

	static String getResponseFor(String message, String username, char[] password, String host, String domain) throws NTLMEngineException {
		String response;
		if ((message != null) && !"".equals(message.trim())) {
			final NTLMEngineImpl.Type2Message t2m = new NTLMEngineImpl.Type2Message(message);
			response = getType3Message(username, password, host, domain, t2m.getChallenge(), t2m.getFlags(), t2m.getTarget(), t2m.getTargetInfo());
		} else {
			response = getType1Message(host, domain);
		}

		return response;
	}

	static String getResponseFor(String message, String username, char[] password, String host, String domain, Certificate peerServerCertificate) throws NTLMEngineException {
		String response;
		if ((message != null) && !"".equals(message.trim())) {
			final NTLMEngineImpl.Type1Message t1m = new NTLMEngineImpl.Type1Message(host, domain);
			final NTLMEngineImpl.Type2Message t2m = new NTLMEngineImpl.Type2Message(message);
			response = getType3Message(username, password, host, domain, t2m.getChallenge(), t2m.getFlags(), t2m.getTarget(), t2m.getTargetInfo(), peerServerCertificate, t1m.getBytes(), t2m.getBytes());
		} else {
			response = (new NTLMEngineImpl.Type1Message(host, domain)).getResponse();
		}

		return response;
	}

	static String getType1Message(String host, String domain) {
		return TYPE_1_MESSAGE;
	}

	static String getType3Message(String user, char[] password, String host, String domain, byte[] nonce, int type2Flags, String target, byte[] targetInformation) throws NTLMEngineException {
		return (new NTLMEngineImpl.Type3Message(domain, host, user, password, nonce, type2Flags, target, targetInformation)).getResponse();
	}

	static String getType3Message(String user, char[] password, String host, String domain, byte[] nonce, int type2Flags, String target, byte[] targetInformation, Certificate peerServerCertificate, byte[] type1Message, byte[] type2Message)
		throws NTLMEngineException {
		return (new NTLMEngineImpl.Type3Message(domain, host, user, password, nonce, type2Flags, target, targetInformation, peerServerCertificate, type1Message, type2Message)).getResponse();
	}

	private static int readULong(byte[] src, int index) {
		return src.length < (index + 4) ? 0 : (src[index] & 255) | ((src[index + 1] & 255) << 8) | ((src[index + 2] & 255) << 16) | ((src[index + 3] & 255) << 24);
	}

	private static int readUShort(byte[] src, int index) {
		return src.length < (index + 2) ? 0 : (src[index] & 255) | ((src[index + 1] & 255) << 8);
	}

	private static byte[] readSecurityBuffer(byte[] src, int index) {
		final int length = readUShort(src, index);
		final int offset = readULong(src, index + 4);
		if (src.length < (offset + length)) {
			return new byte[length];
		}
		final byte[] buffer = new byte[length];
		System.arraycopy(src, offset, buffer, 0, length);
		return buffer;
	}

	private static synchronized byte[] makeRandomChallenge(Random random) {
		final byte[] rval = new byte[8];
		random.nextBytes(rval);
		return rval;
	}

	private static synchronized byte[] makeSecondaryKey(Random random) {
		final byte[] rval = new byte[16];
		random.nextBytes(rval);
		return rval;
	}

	static byte[] hmacMD5(byte[] value, byte[] key) {
		final NTLMEngineImpl.HMACMD5 hmacMD5 = new NTLMEngineImpl.HMACMD5(key);
		hmacMD5.update(value);
		return hmacMD5.getOutput();
	}

	static byte[] RC4(byte[] value, byte[] key) throws NTLMEngineException {
		try {
			final Cipher rc4 = Cipher.getInstance("RC4");
			rc4.init(1, new SecretKeySpec(key, "RC4"));
			return rc4.doFinal(value);
		} catch (final Exception var3) {
			throw new NTLMEngineException(var3.getMessage(), var3);
		}
	}

	static byte[] ntlm2SessionResponse(byte[] ntlmHash, byte[] challenge, byte[] clientChallenge) throws NTLMEngineException {
		try {
			final MessageDigest md5 = getMD5();
			md5.update(challenge);
			md5.update(clientChallenge);
			final byte[] digest = md5.digest();
			final byte[] sessionHash = new byte[8];
			System.arraycopy(digest, 0, sessionHash, 0, 8);
			return lmResponse(ntlmHash, sessionHash);
		} catch (final Exception var6) {
			if (var6 instanceof NTLMEngineException) {
				throw (NTLMEngineException) var6;
			}
			throw new NTLMEngineException(var6.getMessage(), var6);
		}
	}

	private static byte[] lmHash(char[] password) throws NTLMEngineException {
		try {
			final char[] tmp = new char[password.length];

			for (int i = 0; i < password.length; ++i) {
				tmp[i] = Character.toUpperCase(password[i]);
			}

			final byte[] oemPassword = (new ByteArrayBuilder()).append(tmp).toByteArray();
			final int length = Math.min(oemPassword.length, 14);
			final byte[] keyBytes = new byte[14];
			System.arraycopy(oemPassword, 0, keyBytes, 0, length);
			final Key lowKey = createDESKey(keyBytes, 0);
			final Key highKey = createDESKey(keyBytes, 7);
			final byte[] magicConstant = "KGS!@#$%".getBytes(StandardCharsets.US_ASCII);
			final Cipher des = Cipher.getInstance("DES/ECB/NoPadding");
			des.init(1, lowKey);
			final byte[] lowHash = des.doFinal(magicConstant);
			des.init(1, highKey);
			final byte[] highHash = des.doFinal(magicConstant);
			final byte[] lmHash = new byte[16];
			System.arraycopy(lowHash, 0, lmHash, 0, 8);
			System.arraycopy(highHash, 0, lmHash, 8, 8);
			return lmHash;
		} catch (final Exception var12) {
			throw new NTLMEngineException(var12.getMessage(), var12);
		}
	}

	private static byte[] ntlmHash(char[] password) throws NTLMEngineException {
		if (UNICODE_LITTLE_UNMARKED == null) {
			throw new NTLMEngineException("Unicode not supported");
		}
		final byte[] unicodePassword = (new ByteArrayBuilder()).charset(UNICODE_LITTLE_UNMARKED).append(password).toByteArray();
		final NTLMEngineImpl.MD4 md4 = new NTLMEngineImpl.MD4();
		md4.update(unicodePassword);
		return md4.getOutput();
	}

	private static byte[] lmv2Hash(String domain, String user, byte[] ntlmHash) throws NTLMEngineException {
		if (UNICODE_LITTLE_UNMARKED == null) {
			throw new NTLMEngineException("Unicode not supported");
		}
		final NTLMEngineImpl.HMACMD5 hmacMD5 = new NTLMEngineImpl.HMACMD5(ntlmHash);
		hmacMD5.update(user.toUpperCase(Locale.ROOT).getBytes(UNICODE_LITTLE_UNMARKED));
		if (domain != null) {
			hmacMD5.update(domain.toUpperCase(Locale.ROOT).getBytes(UNICODE_LITTLE_UNMARKED));
		}

		return hmacMD5.getOutput();
	}

	private static byte[] ntlmv2Hash(String domain, String user, byte[] ntlmHash) throws NTLMEngineException {
		if (UNICODE_LITTLE_UNMARKED == null) {
			throw new NTLMEngineException("Unicode not supported");
		}
		final NTLMEngineImpl.HMACMD5 hmacMD5 = new NTLMEngineImpl.HMACMD5(ntlmHash);
		hmacMD5.update(user.toUpperCase(Locale.ROOT).getBytes(UNICODE_LITTLE_UNMARKED));
		if (domain != null) {
			hmacMD5.update(domain.getBytes(UNICODE_LITTLE_UNMARKED));
		}

		return hmacMD5.getOutput();
	}

	private static byte[] lmResponse(byte[] hash, byte[] challenge) throws NTLMEngineException {
		try {
			final byte[] keyBytes = new byte[21];
			System.arraycopy(hash, 0, keyBytes, 0, 16);
			final Key lowKey = createDESKey(keyBytes, 0);
			final Key middleKey = createDESKey(keyBytes, 7);
			final Key highKey = createDESKey(keyBytes, 14);
			final Cipher des = Cipher.getInstance("DES/ECB/NoPadding");
			des.init(1, lowKey);
			final byte[] lowResponse = des.doFinal(challenge);
			des.init(1, middleKey);
			final byte[] middleResponse = des.doFinal(challenge);
			des.init(1, highKey);
			final byte[] highResponse = des.doFinal(challenge);
			final byte[] lmResponse = new byte[24];
			System.arraycopy(lowResponse, 0, lmResponse, 0, 8);
			System.arraycopy(middleResponse, 0, lmResponse, 8, 8);
			System.arraycopy(highResponse, 0, lmResponse, 16, 8);
			return lmResponse;
		} catch (final Exception var11) {
			throw new NTLMEngineException(var11.getMessage(), var11);
		}
	}

	private static byte[] lmv2Response(byte[] hash, byte[] challenge, byte[] clientData) {
		final NTLMEngineImpl.HMACMD5 hmacMD5 = new NTLMEngineImpl.HMACMD5(hash);
		hmacMD5.update(challenge);
		hmacMD5.update(clientData);
		final byte[] mac = hmacMD5.getOutput();
		final byte[] lmv2Response = new byte[mac.length + clientData.length];
		System.arraycopy(mac, 0, lmv2Response, 0, mac.length);
		System.arraycopy(clientData, 0, lmv2Response, mac.length, clientData.length);
		return lmv2Response;
	}

	private static byte[] encodeLong(int value) {
		final byte[] enc = new byte[4];
		encodeLong(enc, 0, value);
		return enc;
	}

	private static void encodeLong(byte[] buf, int offset, int value) {
		buf[offset] = (byte) (value & 255);
		buf[offset + 1] = (byte) ((value >> 8) & 255);
		buf[offset + 2] = (byte) ((value >> 16) & 255);
		buf[offset + 3] = (byte) ((value >> 24) & 255);
	}

	private static byte[] createBlob(byte[] clientChallenge, byte[] targetInformation, byte[] timestamp) {
		final byte[] blobSignature = { 1, 1, 0, 0 };
		final byte[] reserved = { 0, 0, 0, 0 };
		final byte[] unknown1 = { 0, 0, 0, 0 };
		final byte[] unknown2 = { 0, 0, 0, 0 };
		final byte[] blob = new byte[blobSignature.length + reserved.length + timestamp.length + 8 + unknown1.length + targetInformation.length + unknown2.length];
		int offset = 0;
		System.arraycopy(blobSignature, 0, blob, offset, blobSignature.length);
		offset += blobSignature.length;
		System.arraycopy(reserved, 0, blob, offset, reserved.length);
		offset += reserved.length;
		System.arraycopy(timestamp, 0, blob, offset, timestamp.length);
		offset += timestamp.length;
		System.arraycopy(clientChallenge, 0, blob, offset, 8);
		offset += 8;
		System.arraycopy(unknown1, 0, blob, offset, unknown1.length);
		offset += unknown1.length;
		System.arraycopy(targetInformation, 0, blob, offset, targetInformation.length);
		offset += targetInformation.length;
		System.arraycopy(unknown2, 0, blob, offset, unknown2.length);
		return blob;
	}

	private static Key createDESKey(byte[] bytes, int offset) {
		final byte[] keyBytes = new byte[7];
		System.arraycopy(bytes, offset, keyBytes, 0, 7);
		final byte[] material = { keyBytes[0], (byte) ((keyBytes[0] << 7) | ((keyBytes[1] & 255) >>> 1)), (byte) ((keyBytes[1] << 6) | ((keyBytes[2] & 255) >>> 2)), (byte) ((keyBytes[2] << 5) | ((keyBytes[3] & 255) >>> 3)), (byte) ((keyBytes[3] << 4)
			| ((keyBytes[4] & 255) >>> 4)), (byte) ((keyBytes[4] << 3) | ((keyBytes[5] & 255) >>> 5)), (byte) ((keyBytes[5] << 2) | ((keyBytes[6] & 255) >>> 6)), (byte) (keyBytes[6] << 1) };
		oddParity(material);
		return new SecretKeySpec(material, "DES");
	}

	private static void oddParity(byte[] bytes) {
		for (int i = 0; i < bytes.length; ++i) {
			final byte b = bytes[i];
			final boolean needsParity = (((b >>> 7) ^ (b >>> 6) ^ (b >>> 5) ^ (b >>> 4) ^ (b >>> 3) ^ (b >>> 2) ^ (b >>> 1)) & 1) == 0;
			if (needsParity) {
				bytes[i] = (byte) (bytes[i] | 1);
			} else {
				bytes[i] &= -2;
			}
		}

	}

	private static Charset getCharset(int flags) throws NTLMEngineException {
		if ((flags & 1) == 0) {
			return DEFAULT_CHARSET;
		}
		if (UNICODE_LITTLE_UNMARKED == null) {
			throw new NTLMEngineException("Unicode not supported");
		}
		return UNICODE_LITTLE_UNMARKED;
	}

	static void writeUShort(byte[] buffer, int value, int offset) {
		buffer[offset] = (byte) (value & 255);
		buffer[offset + 1] = (byte) ((value >> 8) & 255);
	}

	static void writeULong(byte[] buffer, int value, int offset) {
		buffer[offset] = (byte) (value & 255);
		buffer[offset + 1] = (byte) ((value >> 8) & 255);
		buffer[offset + 2] = (byte) ((value >> 16) & 255);
		buffer[offset + 3] = (byte) ((value >> 24) & 255);
	}

	static int F(int x, int y, int z) {
		return (x & y) | (~x & z);
	}

	static int G(int x, int y, int z) {
		return (x & y) | (x & z) | (y & z);
	}

	static int H(int x, int y, int z) {
		return x ^ y ^ z;
	}

	static int rotintlft(int val, int numbits) {
		return (val << numbits) | (val >>> (32 - numbits));
	}

	static MessageDigest getMD5() {
		try {
			return MessageDigest.getInstance("MD5");
		} catch (final NoSuchAlgorithmException var1) {
			throw new RuntimeException("MD5 message digest doesn't seem to exist - fatal error: " + var1.getMessage(), var1);
		}
	}

	@Override
	public String generateType1Msg(String domain, String workstation) throws NTLMEngineException {
		return getType1Message(workstation, domain);
	}

	@Override
	public String generateType3Msg(String username, char[] password, String domain, String workstation, String challenge) throws NTLMEngineException {
		final NTLMEngineImpl.Type2Message t2m = new NTLMEngineImpl.Type2Message(challenge);
		return getType3Message(username, password, workstation, domain, t2m.getChallenge(), t2m.getFlags(), t2m.getTarget(), t2m.getTargetInfo());
	}

	enum Mode {
		CLIENT,
		SERVER;

		Mode() {}
	}

	static class HMACMD5 {
		final byte[] ipad;
		final byte[] opad;
		final MessageDigest md5;

		HMACMD5(byte[] input) {
			byte[] key = input;
			this.md5 = NTLMEngineImpl.getMD5();
			this.ipad = new byte[64];
			this.opad = new byte[64];
			int keyLength = input.length;
			if (keyLength > 64) {
				this.md5.update(input);
				key = this.md5.digest();
				keyLength = key.length;
			}

			int i;
			for (i = 0; i < keyLength; ++i) {
				this.ipad[i] = (byte) (key[i] ^ 54);
				this.opad[i] = (byte) (key[i] ^ 92);
			}

			while (i < 64) {
				this.ipad[i] = 54;
				this.opad[i] = 92;
				++i;
			}

			this.md5.reset();
			this.md5.update(this.ipad);
		}

		byte[] getOutput() {
			final byte[] digest = this.md5.digest();
			this.md5.update(this.opad);
			return this.md5.digest(digest);
		}

		void update(byte[] input) {
			this.md5.update(input);
		}

		void update(byte[] input, int offset, int length) {
			this.md5.update(input, offset, length);
		}
	}

	static class MD4 {
		final byte[] dataBuffer = new byte[64];
		int A = 1732584193;
		int B = -271733879;
		int C = -1732584194;
		int D = 271733878;
		long count;

		MD4() {}

		void update(byte[] input) {
			int curBufferPos = (int) (this.count & 63L);
			int inputIndex = 0;

			int transferAmt;
			while (((input.length - inputIndex) + curBufferPos) >= this.dataBuffer.length) {
				transferAmt = this.dataBuffer.length - curBufferPos;
				System.arraycopy(input, inputIndex, this.dataBuffer, curBufferPos, transferAmt);
				this.count += transferAmt;
				curBufferPos = 0;
				inputIndex += transferAmt;
				this.processBuffer();
			}

			if (inputIndex < input.length) {
				transferAmt = input.length - inputIndex;
				System.arraycopy(input, inputIndex, this.dataBuffer, curBufferPos, transferAmt);
				this.count += transferAmt;
			}
		}

		byte[] getOutput() {
			final int bufferIndex = (int) (this.count & 63L);
			final int padLen = bufferIndex < 56 ? 56 - bufferIndex : 120 - bufferIndex;
			final byte[] postBytes = new byte[padLen + 8];
			postBytes[0] = -128;

			for (int i = 0; i < 8; ++i) {
				postBytes[padLen + i] = (byte) ((int) ((this.count * 8L) >>> (8 * i)));
			}

			this.update(postBytes);
			final byte[] result = new byte[16];
			NTLMEngineImpl.writeULong(result, this.A, 0);
			NTLMEngineImpl.writeULong(result, this.B, 4);
			NTLMEngineImpl.writeULong(result, this.C, 8);
			NTLMEngineImpl.writeULong(result, this.D, 12);
			return result;
		}

		void processBuffer() {
			final int[] d = new int[16];

			int AA;
			for (AA = 0; AA < 16; ++AA) {
				d[AA] = (this.dataBuffer[AA * 4] & 255) + ((this.dataBuffer[(AA * 4) + 1] & 255) << 8) + ((this.dataBuffer[(AA * 4) + 2] & 255) << 16) + ((this.dataBuffer[(AA * 4) + 3] & 255) << 24);
			}

			AA = this.A;
			final int BB = this.B;
			final int CC = this.C;
			final int DD = this.D;
			this.round1(d);
			this.round2(d);
			this.round3(d);
			this.A += AA;
			this.B += BB;
			this.C += CC;
			this.D += DD;
		}

		void round1(int[] d) {
			this.A = NTLMEngineImpl.rotintlft(this.A + NTLMEngineImpl.F(this.B, this.C, this.D) + d[0], 3);
			this.D = NTLMEngineImpl.rotintlft(this.D + NTLMEngineImpl.F(this.A, this.B, this.C) + d[1], 7);
			this.C = NTLMEngineImpl.rotintlft(this.C + NTLMEngineImpl.F(this.D, this.A, this.B) + d[2], 11);
			this.B = NTLMEngineImpl.rotintlft(this.B + NTLMEngineImpl.F(this.C, this.D, this.A) + d[3], 19);
			this.A = NTLMEngineImpl.rotintlft(this.A + NTLMEngineImpl.F(this.B, this.C, this.D) + d[4], 3);
			this.D = NTLMEngineImpl.rotintlft(this.D + NTLMEngineImpl.F(this.A, this.B, this.C) + d[5], 7);
			this.C = NTLMEngineImpl.rotintlft(this.C + NTLMEngineImpl.F(this.D, this.A, this.B) + d[6], 11);
			this.B = NTLMEngineImpl.rotintlft(this.B + NTLMEngineImpl.F(this.C, this.D, this.A) + d[7], 19);
			this.A = NTLMEngineImpl.rotintlft(this.A + NTLMEngineImpl.F(this.B, this.C, this.D) + d[8], 3);
			this.D = NTLMEngineImpl.rotintlft(this.D + NTLMEngineImpl.F(this.A, this.B, this.C) + d[9], 7);
			this.C = NTLMEngineImpl.rotintlft(this.C + NTLMEngineImpl.F(this.D, this.A, this.B) + d[10], 11);
			this.B = NTLMEngineImpl.rotintlft(this.B + NTLMEngineImpl.F(this.C, this.D, this.A) + d[11], 19);
			this.A = NTLMEngineImpl.rotintlft(this.A + NTLMEngineImpl.F(this.B, this.C, this.D) + d[12], 3);
			this.D = NTLMEngineImpl.rotintlft(this.D + NTLMEngineImpl.F(this.A, this.B, this.C) + d[13], 7);
			this.C = NTLMEngineImpl.rotintlft(this.C + NTLMEngineImpl.F(this.D, this.A, this.B) + d[14], 11);
			this.B = NTLMEngineImpl.rotintlft(this.B + NTLMEngineImpl.F(this.C, this.D, this.A) + d[15], 19);
		}

		void round2(int[] d) {
			this.A = NTLMEngineImpl.rotintlft(this.A + NTLMEngineImpl.G(this.B, this.C, this.D) + d[0] + 1518500249, 3);
			this.D = NTLMEngineImpl.rotintlft(this.D + NTLMEngineImpl.G(this.A, this.B, this.C) + d[4] + 1518500249, 5);
			this.C = NTLMEngineImpl.rotintlft(this.C + NTLMEngineImpl.G(this.D, this.A, this.B) + d[8] + 1518500249, 9);
			this.B = NTLMEngineImpl.rotintlft(this.B + NTLMEngineImpl.G(this.C, this.D, this.A) + d[12] + 1518500249, 13);
			this.A = NTLMEngineImpl.rotintlft(this.A + NTLMEngineImpl.G(this.B, this.C, this.D) + d[1] + 1518500249, 3);
			this.D = NTLMEngineImpl.rotintlft(this.D + NTLMEngineImpl.G(this.A, this.B, this.C) + d[5] + 1518500249, 5);
			this.C = NTLMEngineImpl.rotintlft(this.C + NTLMEngineImpl.G(this.D, this.A, this.B) + d[9] + 1518500249, 9);
			this.B = NTLMEngineImpl.rotintlft(this.B + NTLMEngineImpl.G(this.C, this.D, this.A) + d[13] + 1518500249, 13);
			this.A = NTLMEngineImpl.rotintlft(this.A + NTLMEngineImpl.G(this.B, this.C, this.D) + d[2] + 1518500249, 3);
			this.D = NTLMEngineImpl.rotintlft(this.D + NTLMEngineImpl.G(this.A, this.B, this.C) + d[6] + 1518500249, 5);
			this.C = NTLMEngineImpl.rotintlft(this.C + NTLMEngineImpl.G(this.D, this.A, this.B) + d[10] + 1518500249, 9);
			this.B = NTLMEngineImpl.rotintlft(this.B + NTLMEngineImpl.G(this.C, this.D, this.A) + d[14] + 1518500249, 13);
			this.A = NTLMEngineImpl.rotintlft(this.A + NTLMEngineImpl.G(this.B, this.C, this.D) + d[3] + 1518500249, 3);
			this.D = NTLMEngineImpl.rotintlft(this.D + NTLMEngineImpl.G(this.A, this.B, this.C) + d[7] + 1518500249, 5);
			this.C = NTLMEngineImpl.rotintlft(this.C + NTLMEngineImpl.G(this.D, this.A, this.B) + d[11] + 1518500249, 9);
			this.B = NTLMEngineImpl.rotintlft(this.B + NTLMEngineImpl.G(this.C, this.D, this.A) + d[15] + 1518500249, 13);
		}

		void round3(int[] d) {
			this.A = NTLMEngineImpl.rotintlft(this.A + NTLMEngineImpl.H(this.B, this.C, this.D) + d[0] + 1859775393, 3);
			this.D = NTLMEngineImpl.rotintlft(this.D + NTLMEngineImpl.H(this.A, this.B, this.C) + d[8] + 1859775393, 9);
			this.C = NTLMEngineImpl.rotintlft(this.C + NTLMEngineImpl.H(this.D, this.A, this.B) + d[4] + 1859775393, 11);
			this.B = NTLMEngineImpl.rotintlft(this.B + NTLMEngineImpl.H(this.C, this.D, this.A) + d[12] + 1859775393, 15);
			this.A = NTLMEngineImpl.rotintlft(this.A + NTLMEngineImpl.H(this.B, this.C, this.D) + d[2] + 1859775393, 3);
			this.D = NTLMEngineImpl.rotintlft(this.D + NTLMEngineImpl.H(this.A, this.B, this.C) + d[10] + 1859775393, 9);
			this.C = NTLMEngineImpl.rotintlft(this.C + NTLMEngineImpl.H(this.D, this.A, this.B) + d[6] + 1859775393, 11);
			this.B = NTLMEngineImpl.rotintlft(this.B + NTLMEngineImpl.H(this.C, this.D, this.A) + d[14] + 1859775393, 15);
			this.A = NTLMEngineImpl.rotintlft(this.A + NTLMEngineImpl.H(this.B, this.C, this.D) + d[1] + 1859775393, 3);
			this.D = NTLMEngineImpl.rotintlft(this.D + NTLMEngineImpl.H(this.A, this.B, this.C) + d[9] + 1859775393, 9);
			this.C = NTLMEngineImpl.rotintlft(this.C + NTLMEngineImpl.H(this.D, this.A, this.B) + d[5] + 1859775393, 11);
			this.B = NTLMEngineImpl.rotintlft(this.B + NTLMEngineImpl.H(this.C, this.D, this.A) + d[13] + 1859775393, 15);
			this.A = NTLMEngineImpl.rotintlft(this.A + NTLMEngineImpl.H(this.B, this.C, this.D) + d[3] + 1859775393, 3);
			this.D = NTLMEngineImpl.rotintlft(this.D + NTLMEngineImpl.H(this.A, this.B, this.C) + d[11] + 1859775393, 9);
			this.C = NTLMEngineImpl.rotintlft(this.C + NTLMEngineImpl.H(this.D, this.A, this.B) + d[7] + 1859775393, 11);
			this.B = NTLMEngineImpl.rotintlft(this.B + NTLMEngineImpl.H(this.C, this.D, this.A) + d[15] + 1859775393, 15);
		}
	}

	static class Type3Message extends NTLMEngineImpl.NTLMMessage {
		final byte[] type1Message;
		final byte[] type2Message;
		final int type2Flags;
		final byte[] domainBytes;
		final byte[] hostBytes;
		final byte[] userBytes;
		final byte[] sessionKey;
		@Getter
		final byte[] exportedSessionKey;
		final boolean computeMic;
		byte[] lmResp;
		byte[] ntResp;

		Type3Message(String domain, String host, String user, char[] password, byte[] nonce, int type2Flags, String target, byte[] targetInformation) throws NTLMEngineException {
			this(domain, host, user, password, nonce, type2Flags, target, targetInformation, null, null, null);
		}

		Type3Message(Random random, long currentTime, String domain, String host, String user, char[] password, byte[] nonce, int type2Flags, String target, byte[] targetInformation) throws NTLMEngineException {
			this(random, currentTime, domain, host, user, password, nonce, type2Flags, target, targetInformation, null, null, null);
		}

		Type3Message(String domain, String host, String user, char[] password, byte[] nonce, int type2Flags, String target, byte[] targetInformation, Certificate peerServerCertificate, byte[] type1Message, byte[] type2Message) throws NTLMEngineException {
			this(NTLMEngineImpl.RND_GEN, System.currentTimeMillis(), domain, host, user, password, nonce, type2Flags, target, targetInformation, peerServerCertificate, type1Message, type2Message);
		}

		Type3Message(Random random, long currentTime, String domain, String host, String user, char[] password, byte[] nonce, int type2Flags, String target, byte[] targetInformation, Certificate peerServerCertificate, byte[] type1Message,
			byte[] type2Message) throws NTLMEngineException {
			if (random == null) {
				throw new NTLMEngineException("Random generator not available");
			}
			this.type2Flags = type2Flags;
			this.type1Message = type1Message;
			this.type2Message = type2Message;
			byte[] responseTargetInformation = targetInformation;
			if (peerServerCertificate != null) {
				responseTargetInformation = this.addGssMicAvsToTargetInfo(targetInformation, peerServerCertificate);
				this.computeMic = true;
			} else {
				this.computeMic = false;
			}

			final NTLMEngineImpl.CipherGen gen = new NTLMEngineImpl.CipherGen(random, currentTime, domain, user, password, nonce, target, responseTargetInformation);

			byte[] userSessionKey;
			try {
				if (((type2Flags & 8388608) != 0) && (targetInformation != null) && (target != null)) {
					this.ntResp = gen.getNTLMv2Response();
					this.lmResp = gen.getLMv2Response();
					if ((type2Flags & 128) != 0) {
						userSessionKey = gen.getLanManagerSessionKey();
					} else {
						userSessionKey = gen.getNTLMv2UserSessionKey();
					}
				} else if ((type2Flags & 524288) != 0) {
					this.ntResp = gen.getNTLM2SessionResponse();
					this.lmResp = gen.getLM2SessionResponse();
					if ((type2Flags & 128) != 0) {
						userSessionKey = gen.getLanManagerSessionKey();
					} else {
						userSessionKey = gen.getNTLM2SessionResponseUserSessionKey();
					}
				} else {
					this.ntResp = gen.getNTLMResponse();
					this.lmResp = gen.getLMResponse();
					if ((type2Flags & 128) != 0) {
						userSessionKey = gen.getLanManagerSessionKey();
					} else {
						userSessionKey = gen.getNTLMUserSessionKey();
					}
				}
			} catch (final NTLMEngineException var21) {
				this.ntResp = new byte[0];
				this.lmResp = gen.getLMResponse();
				if ((type2Flags & 128) != 0) {
					userSessionKey = gen.getLanManagerSessionKey();
				} else {
					userSessionKey = gen.getLMUserSessionKey();
				}
			}

			if ((type2Flags & 16) != 0) {
				if ((type2Flags & 1073741824) != 0) {
					this.exportedSessionKey = gen.getSecondaryKey();
					this.sessionKey = NTLMEngineImpl.RC4(this.exportedSessionKey, userSessionKey);
				} else {
					this.sessionKey = userSessionKey;
					this.exportedSessionKey = this.sessionKey;
				}
			} else {
				if (this.computeMic) {
					throw new NTLMEngineException("Cannot sign/seal: no exported session key");
				}

				this.sessionKey = null;
				this.exportedSessionKey = null;
			}

			final Charset charset = NTLMEngineImpl.getCharset(type2Flags);
			this.hostBytes = host != null ? host.getBytes(charset) : null;
			this.domainBytes = domain != null ? domain.toUpperCase(Locale.ROOT).getBytes(charset) : null;
			this.userBytes = user.getBytes(charset);
		}

		public byte[] getEncryptedRandomSessionKey() {
			return this.sessionKey;
		}

		@Override
		void buildMessage() {
			final int ntRespLen = this.ntResp.length;
			final int lmRespLen = this.lmResp.length;
			final int domainLen = this.domainBytes != null ? this.domainBytes.length : 0;
			final int hostLen = this.hostBytes != null ? this.hostBytes.length : 0;
			final int userLen = this.userBytes.length;
			int sessionKeyLen;
			if (this.sessionKey != null) {
				sessionKeyLen = this.sessionKey.length;
			} else {
				sessionKeyLen = 0;
			}

			final int lmRespOffset = 72 + (this.computeMic ? 16 : 0);
			final int ntRespOffset = lmRespOffset + lmRespLen;
			final int domainOffset = ntRespOffset + ntRespLen;
			final int userOffset = domainOffset + domainLen;
			final int hostOffset = userOffset + userLen;
			final int sessionKeyOffset = hostOffset + hostLen;
			final int finalLength = sessionKeyOffset + sessionKeyLen;
			this.prepareResponse(finalLength, 3);
			this.addUShort(lmRespLen);
			this.addUShort(lmRespLen);
			this.addULong(lmRespOffset);
			this.addUShort(ntRespLen);
			this.addUShort(ntRespLen);
			this.addULong(ntRespOffset);
			this.addUShort(domainLen);
			this.addUShort(domainLen);
			this.addULong(domainOffset);
			this.addUShort(userLen);
			this.addUShort(userLen);
			this.addULong(userOffset);
			this.addUShort(hostLen);
			this.addUShort(hostLen);
			this.addULong(hostOffset);
			this.addUShort(sessionKeyLen);
			this.addUShort(sessionKeyLen);
			this.addULong(sessionKeyOffset);
			this.addULong(this.type2Flags);
			this.addUShort(261);
			this.addULong(2600);
			this.addUShort(3840);
			int micPosition = -1;
			if (this.computeMic) {
				micPosition = this.currentOutputPosition;
				this.currentOutputPosition += 16;
			}

			this.addBytes(this.lmResp);
			this.addBytes(this.ntResp);
			this.addBytes(this.domainBytes);
			this.addBytes(this.userBytes);
			this.addBytes(this.hostBytes);
			if (this.sessionKey != null) {
				this.addBytes(this.sessionKey);
			}

			if (this.computeMic) {
				final NTLMEngineImpl.HMACMD5 hmacMD5 = new NTLMEngineImpl.HMACMD5(this.exportedSessionKey);
				hmacMD5.update(this.type1Message);
				hmacMD5.update(this.type2Message);
				hmacMD5.update(this.messageContents);
				final byte[] mic = hmacMD5.getOutput();
				System.arraycopy(mic, 0, this.messageContents, micPosition, mic.length);
			}

		}

		private byte[] addGssMicAvsToTargetInfo(byte[] originalTargetInfo, Certificate peerServerCertificate) throws NTLMEngineException {
			final byte[] newTargetInfo = new byte[originalTargetInfo.length + 8 + 20];
			final int appendLength = originalTargetInfo.length - 4;
			System.arraycopy(originalTargetInfo, 0, newTargetInfo, 0, appendLength);
			NTLMEngineImpl.writeUShort(newTargetInfo, 6, appendLength);
			NTLMEngineImpl.writeUShort(newTargetInfo, 4, appendLength + 2);
			NTLMEngineImpl.writeULong(newTargetInfo, 2, appendLength + 4);
			NTLMEngineImpl.writeUShort(newTargetInfo, 10, appendLength + 8);
			NTLMEngineImpl.writeUShort(newTargetInfo, 16, appendLength + 10);

			byte[] channelBindingsHash;
			try {
				final byte[] certBytes = peerServerCertificate.getEncoded();
				final MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
				final byte[] certHashBytes = sha256.digest(certBytes);
				final byte[] channelBindingStruct = new byte[20 + NTLMEngineImpl.MAGIC_TLS_SERVER_ENDPOINT.length + certHashBytes.length];
				NTLMEngineImpl.writeULong(channelBindingStruct, 53, 16);
				System.arraycopy(NTLMEngineImpl.MAGIC_TLS_SERVER_ENDPOINT, 0, channelBindingStruct, 20, NTLMEngineImpl.MAGIC_TLS_SERVER_ENDPOINT.length);
				System.arraycopy(certHashBytes, 0, channelBindingStruct, 20 + NTLMEngineImpl.MAGIC_TLS_SERVER_ENDPOINT.length, certHashBytes.length);
				final MessageDigest md5 = NTLMEngineImpl.getMD5();
				channelBindingsHash = md5.digest(channelBindingStruct);
			} catch (NoSuchAlgorithmException | CertificateEncodingException var11) {
				throw new NTLMEngineException(var11.getMessage(), var11);
			}

			System.arraycopy(channelBindingsHash, 0, newTargetInfo, appendLength + 12, 16);
			return newTargetInfo;
		}
	}

	static class Type2Message extends NTLMEngineImpl.NTLMMessage {
		final byte[] challenge;
		final int flags;
		String target;
		byte[] targetInfo;

		Type2Message(String messageBody) throws NTLMEngineException {
			this(Base64.decodeBase64(messageBody.getBytes(NTLMEngineImpl.DEFAULT_CHARSET)));
		}

		Type2Message(byte[] message) throws NTLMEngineException {
			super(message, 2);
			this.challenge = new byte[8];
			this.readBytes(this.challenge, 24);
			this.flags = this.readULong(20);
			this.target = null;
			byte[] bytes;
			if (this.getMessageLength() >= 20) {
				bytes = this.readSecurityBuffer(12);
				if (bytes.length != 0) {
					this.target = new String(bytes, NTLMEngineImpl.getCharset(this.flags));
				}
			}

			this.targetInfo = null;
			if (this.getMessageLength() >= 48) {
				bytes = this.readSecurityBuffer(40);
				if (bytes.length != 0) {
					this.targetInfo = bytes;
				}
			}

		}

		byte[] getChallenge() {
			return this.challenge;
		}

		String getTarget() {
			return this.target;
		}

		byte[] getTargetInfo() {
			return this.targetInfo;
		}

		int getFlags() {
			return this.flags;
		}
	}

	static class Type1Message extends NTLMEngineImpl.NTLMMessage {
		private final byte[] hostBytes;
		private final byte[] domainBytes;
		private final int flags;

		Type1Message(String domain, String host) {
			this(domain, host, null);
		}

		Type1Message(String domain, String host, Integer flags) {
			this.flags = flags == null ? this.getDefaultFlags() : flags;
			this.hostBytes = host != null ? host.getBytes(NTLMEngineImpl.UNICODE_LITTLE_UNMARKED) : null;
			this.domainBytes = domain != null ? domain.toUpperCase(Locale.ROOT).getBytes(NTLMEngineImpl.UNICODE_LITTLE_UNMARKED) : null;
		}

		Type1Message() {
			this.hostBytes = null;
			this.domainBytes = null;
			this.flags = this.getDefaultFlags();
		}

		private int getDefaultFlags() {
			return -1576500735;
		}

		@Override
		void buildMessage() {
			int domainBytesLength = 0;
			if (this.domainBytes != null) {
				domainBytesLength = this.domainBytes.length;
			}

			int hostBytesLength = 0;
			if (this.hostBytes != null) {
				hostBytesLength = this.hostBytes.length;
			}

			final int finalLength = 40 + hostBytesLength + domainBytesLength;
			this.prepareResponse(finalLength, 1);
			this.addULong(this.flags);
			this.addUShort(domainBytesLength);
			this.addUShort(domainBytesLength);
			this.addULong(hostBytesLength + 32 + 8);
			this.addUShort(hostBytesLength);
			this.addUShort(hostBytesLength);
			this.addULong(40);
			this.addUShort(261);
			this.addULong(2600);
			this.addUShort(3840);
			if (this.hostBytes != null) {
				this.addBytes(this.hostBytes);
			}

			if (this.domainBytes != null) {
				this.addBytes(this.domainBytes);
			}

		}
	}

	static class NTLMMessage {
		byte[] messageContents;
		int currentOutputPosition;

		NTLMMessage() {}

		NTLMMessage(String messageBody, int expectedType) throws NTLMEngineException {
			this(Base64.decodeBase64(messageBody.getBytes(NTLMEngineImpl.DEFAULT_CHARSET)), expectedType);
		}

		NTLMMessage(byte[] message, int expectedType) throws NTLMEngineException {
			this.messageContents = message;
			if (this.messageContents.length < NTLMEngineImpl.SIGNATURE.length) {
				throw new NTLMEngineException("NTLM message decoding error - packet too short");
			}
			for (int i = 0; i < NTLMEngineImpl.SIGNATURE.length; ++i) {
				if (this.messageContents[i] != NTLMEngineImpl.SIGNATURE[i]) {
					throw new NTLMEngineException("NTLM message expected - instead got unrecognized bytes");
				}
			}

			final int type = this.readULong(NTLMEngineImpl.SIGNATURE.length);
			if (type != expectedType) {
				throw new NTLMEngineException("NTLM type " + expectedType + " message expected - instead got type " + type);
			}
			this.currentOutputPosition = this.messageContents.length;
		}

		int getPreambleLength() {
			return NTLMEngineImpl.SIGNATURE.length + 4;
		}

		int getMessageLength() {
			return this.currentOutputPosition;
		}

		byte readByte(int position) throws NTLMEngineException {
			if (this.messageContents.length < (position + 1)) {
				throw new NTLMEngineException("NTLM: Message too short");
			}
			return this.messageContents[position];
		}

		void readBytes(byte[] buffer, int position) throws NTLMEngineException {
			if (this.messageContents.length < (position + buffer.length)) {
				throw new NTLMEngineException("NTLM: Message too short");
			}
			System.arraycopy(this.messageContents, position, buffer, 0, buffer.length);
		}

		int readUShort(int position) {
			return NTLMEngineImpl.readUShort(this.messageContents, position);
		}

		int readULong(int position) {
			return NTLMEngineImpl.readULong(this.messageContents, position);
		}

		byte[] readSecurityBuffer(int position) {
			return NTLMEngineImpl.readSecurityBuffer(this.messageContents, position);
		}

		void prepareResponse(int maxlength, int messageType) {
			this.messageContents = new byte[maxlength];
			this.currentOutputPosition = 0;
			this.addBytes(NTLMEngineImpl.SIGNATURE);
			this.addULong(messageType);
		}

		void addByte(byte b) {
			this.messageContents[this.currentOutputPosition] = b;
			++this.currentOutputPosition;
		}

		void addBytes(byte[] bytes) {
			if (bytes != null) {
				final byte[] arr$ = bytes;
				final int len$ = bytes.length;

				for (int i$ = 0; i$ < len$; ++i$) {
					final byte b = arr$[i$];
					this.messageContents[this.currentOutputPosition] = b;
					++this.currentOutputPosition;
				}

			}
		}

		void addUShort(int value) {
			this.addByte((byte) (value & 255));
			this.addByte((byte) ((value >> 8) & 255));
		}

		void addULong(int value) {
			this.addByte((byte) (value & 255));
			this.addByte((byte) ((value >> 8) & 255));
			this.addByte((byte) ((value >> 16) & 255));
			this.addByte((byte) ((value >> 24) & 255));
		}

		public String getResponse() {
			return new String(Base64.encodeBase64(this.getBytes()), StandardCharsets.US_ASCII);
		}

		public byte[] getBytes() {
			if (this.messageContents == null) {
				this.buildMessage();
			}

			if (this.messageContents.length > this.currentOutputPosition) {
				final byte[] tmp = new byte[this.currentOutputPosition];
				System.arraycopy(this.messageContents, 0, tmp, 0, this.currentOutputPosition);
				this.messageContents = tmp;
			}

			return this.messageContents;
		}

		void buildMessage() {
			throw new RuntimeException("Message builder not implemented for " + this.getClass().getName());
		}
	}

	static class Handle {
		final NTLMEngineImpl.Mode mode;
		@Getter
		private final byte[] signingKey;
		private final Cipher rc4;
		private final boolean isConnection;
		int sequenceNumber;
		@Getter
		private byte[] sealingKey;

		Handle(byte[] exportedSessionKey, NTLMEngineImpl.Mode mode, boolean isConnection) throws NTLMEngineException {
			this.isConnection = isConnection;
			this.mode = mode;

			try {
				final MessageDigest signMd5 = NTLMEngineImpl.getMD5();
				final MessageDigest sealMd5 = NTLMEngineImpl.getMD5();
				signMd5.update(exportedSessionKey);
				sealMd5.update(exportedSessionKey);
				if (mode == NTLMEngineImpl.Mode.CLIENT) {
					signMd5.update(NTLMEngineImpl.SIGN_MAGIC_CLIENT);
					sealMd5.update(NTLMEngineImpl.SEAL_MAGIC_CLIENT);
				} else {
					signMd5.update(NTLMEngineImpl.SIGN_MAGIC_SERVER);
					sealMd5.update(NTLMEngineImpl.SEAL_MAGIC_SERVER);
				}

				this.signingKey = signMd5.digest();
				this.sealingKey = sealMd5.digest();
			} catch (final Exception var6) {
				throw new NTLMEngineException(var6.getMessage(), var6);
			}

			this.rc4 = this.initCipher();
		}

		private Cipher initCipher() throws NTLMEngineException {
			try {
				final Cipher cipher = Cipher.getInstance("RC4");
				if (this.mode == NTLMEngineImpl.Mode.CLIENT) {
					cipher.init(1, new SecretKeySpec(this.sealingKey, "RC4"));
				} else {
					cipher.init(2, new SecretKeySpec(this.sealingKey, "RC4"));
				}

				return cipher;
			} catch (final Exception var3) {
				throw new NTLMEngineException(var3.getMessage(), var3);
			}
		}

		private void advanceMessageSequence() throws NTLMEngineException {
			if (!this.isConnection) {
				final MessageDigest sealMd5 = NTLMEngineImpl.getMD5();
				sealMd5.update(this.sealingKey);
				final byte[] seqNumBytes = new byte[4];
				NTLMEngineImpl.writeULong(seqNumBytes, this.sequenceNumber, 0);
				sealMd5.update(seqNumBytes);
				this.sealingKey = sealMd5.digest();
				this.initCipher();
			}

			++this.sequenceNumber;
		}

		private byte[] encrypt(byte[] data) {
			return this.rc4.update(data);
		}

		private byte[] decrypt(byte[] data) {
			return this.rc4.update(data);
		}

		private byte[] computeSignature(byte[] message) {
			final byte[] sig = new byte[16];
			sig[0] = 1;
			sig[1] = 0;
			sig[2] = 0;
			sig[3] = 0;
			final NTLMEngineImpl.HMACMD5 hmacMD5 = new NTLMEngineImpl.HMACMD5(this.signingKey);
			hmacMD5.update(NTLMEngineImpl.encodeLong(this.sequenceNumber));
			hmacMD5.update(message);
			final byte[] hmac = hmacMD5.getOutput();
			final byte[] trimmedHmac = new byte[8];
			System.arraycopy(hmac, 0, trimmedHmac, 0, 8);
			final byte[] encryptedHmac = this.encrypt(trimmedHmac);
			System.arraycopy(encryptedHmac, 0, sig, 4, 8);
			NTLMEngineImpl.encodeLong(sig, 12, this.sequenceNumber);
			return sig;
		}

		private boolean validateSignature(byte[] signature, byte[] message) {
			final byte[] computedSignature = this.computeSignature(message);
			return Arrays.equals(signature, computedSignature);
		}

		public byte[] signAndEncryptMessage(byte[] cleartextMessage) throws NTLMEngineException {
			final byte[] encryptedMessage = this.encrypt(cleartextMessage);
			final byte[] signature = this.computeSignature(cleartextMessage);
			final byte[] outMessage = new byte[signature.length + encryptedMessage.length];
			System.arraycopy(signature, 0, outMessage, 0, signature.length);
			System.arraycopy(encryptedMessage, 0, outMessage, signature.length, encryptedMessage.length);
			this.advanceMessageSequence();
			return outMessage;
		}

		public byte[] decryptAndVerifySignedMessage(byte[] inMessage) throws NTLMEngineException {
			final byte[] signature = new byte[16];
			System.arraycopy(inMessage, 0, signature, 0, signature.length);
			final byte[] encryptedMessage = new byte[inMessage.length - 16];
			System.arraycopy(inMessage, 16, encryptedMessage, 0, encryptedMessage.length);
			final byte[] cleartextMessage = this.decrypt(encryptedMessage);
			if (!this.validateSignature(signature, cleartextMessage)) {
				throw new NTLMEngineException("Wrong signature");
			}
			this.advanceMessageSequence();
			return cleartextMessage;
		}
	}

	static class CipherGen {
		final Random random;
		final long currentTime;
		final String domain;
		final String user;
		final char[] password;
		final byte[] challenge;
		final String target;
		final byte[] targetInformation;
		byte[] clientChallenge;
		byte[] clientChallenge2;
		byte[] secondaryKey;
		byte[] timestamp;
		byte[] lmHash;
		byte[] lmResponse;
		byte[] ntlmHash;
		byte[] ntlmResponse;
		byte[] ntlmv2Hash;
		byte[] lmv2Hash;
		byte[] lmv2Response;
		byte[] ntlmv2Blob;
		byte[] ntlmv2Response;
		byte[] ntlm2SessionResponse;
		byte[] lm2SessionResponse;
		byte[] lmUserSessionKey;
		byte[] ntlmUserSessionKey;
		byte[] ntlmv2UserSessionKey;
		byte[] ntlm2SessionResponseUserSessionKey;
		byte[] lanManagerSessionKey;

		public CipherGen(Random random, long currentTime, String domain, String user, char[] password, byte[] challenge, String target, byte[] targetInformation, byte[] clientChallenge, byte[] clientChallenge2, byte[] secondaryKey, byte[] timestamp) {
			this.random = random;
			this.currentTime = currentTime;
			this.domain = domain;
			this.target = target;
			this.user = user;
			this.password = password;
			this.challenge = challenge;
			this.targetInformation = targetInformation;
			this.clientChallenge = clientChallenge;
			this.clientChallenge2 = clientChallenge2;
			this.secondaryKey = secondaryKey;
			this.timestamp = timestamp;
		}

		public CipherGen(Random random, long currentTime, String domain, String user, char[] password, byte[] challenge, String target, byte[] targetInformation) {
			this(random, currentTime, domain, user, password, challenge, target, targetInformation, null, null, null, null);
		}

		public byte[] getClientChallenge() {
			if (this.clientChallenge == null) {
				this.clientChallenge = NTLMEngineImpl.makeRandomChallenge(this.random);
			}

			return this.clientChallenge;
		}

		public byte[] getClientChallenge2() {
			if (this.clientChallenge2 == null) {
				this.clientChallenge2 = NTLMEngineImpl.makeRandomChallenge(this.random);
			}

			return this.clientChallenge2;
		}

		public byte[] getSecondaryKey() {
			if (this.secondaryKey == null) {
				this.secondaryKey = NTLMEngineImpl.makeSecondaryKey(this.random);
			}

			return this.secondaryKey;
		}

		public byte[] getLMHash() throws NTLMEngineException {
			if (this.lmHash == null) {
				this.lmHash = NTLMEngineImpl.lmHash(this.password);
			}

			return this.lmHash;
		}

		public byte[] getLMResponse() throws NTLMEngineException {
			if (this.lmResponse == null) {
				this.lmResponse = NTLMEngineImpl.lmResponse(this.getLMHash(), this.challenge);
			}

			return this.lmResponse;
		}

		public byte[] getNTLMHash() throws NTLMEngineException {
			if (this.ntlmHash == null) {
				this.ntlmHash = NTLMEngineImpl.ntlmHash(this.password);
			}

			return this.ntlmHash;
		}

		public byte[] getNTLMResponse() throws NTLMEngineException {
			if (this.ntlmResponse == null) {
				this.ntlmResponse = NTLMEngineImpl.lmResponse(this.getNTLMHash(), this.challenge);
			}

			return this.ntlmResponse;
		}

		public byte[] getLMv2Hash() throws NTLMEngineException {
			if (this.lmv2Hash == null) {
				this.lmv2Hash = NTLMEngineImpl.lmv2Hash(this.domain, this.user, this.getNTLMHash());
			}

			return this.lmv2Hash;
		}

		public byte[] getNTLMv2Hash() throws NTLMEngineException {
			if (this.ntlmv2Hash == null) {
				this.ntlmv2Hash = NTLMEngineImpl.ntlmv2Hash(this.domain, this.user, this.getNTLMHash());
			}

			return this.ntlmv2Hash;
		}

		public byte[] getTimestamp() {
			if (this.timestamp == null) {
				long time = this.currentTime;
				time += 11644473600000L;
				time *= 10000L;
				this.timestamp = new byte[8];

				for (int i = 0; i < 8; ++i) {
					this.timestamp[i] = (byte) ((int) time);
					time >>>= 8;
				}
			}

			return this.timestamp;
		}

		public byte[] getNTLMv2Blob() {
			if (this.ntlmv2Blob == null) {
				this.ntlmv2Blob = NTLMEngineImpl.createBlob(this.getClientChallenge2(), this.targetInformation, this.getTimestamp());
			}

			return this.ntlmv2Blob;
		}

		public byte[] getNTLMv2Response() throws NTLMEngineException {
			if (this.ntlmv2Response == null) {
				this.ntlmv2Response = NTLMEngineImpl.lmv2Response(this.getNTLMv2Hash(), this.challenge, this.getNTLMv2Blob());
			}

			return this.ntlmv2Response;
		}

		public byte[] getLMv2Response() throws NTLMEngineException {
			if (this.lmv2Response == null) {
				this.lmv2Response = NTLMEngineImpl.lmv2Response(this.getLMv2Hash(), this.challenge, this.getClientChallenge());
			}

			return this.lmv2Response;
		}

		public byte[] getNTLM2SessionResponse() throws NTLMEngineException {
			if (this.ntlm2SessionResponse == null) {
				this.ntlm2SessionResponse = NTLMEngineImpl.ntlm2SessionResponse(this.getNTLMHash(), this.challenge, this.getClientChallenge());
			}

			return this.ntlm2SessionResponse;
		}

		public byte[] getLM2SessionResponse() {
			if (this.lm2SessionResponse == null) {
				final byte[] clntChallenge = this.getClientChallenge();
				this.lm2SessionResponse = new byte[24];
				System.arraycopy(clntChallenge, 0, this.lm2SessionResponse, 0, clntChallenge.length);
				Arrays.fill(this.lm2SessionResponse, clntChallenge.length, this.lm2SessionResponse.length, (byte) 0);
			}

			return this.lm2SessionResponse;
		}

		public byte[] getLMUserSessionKey() throws NTLMEngineException {
			if (this.lmUserSessionKey == null) {
				this.lmUserSessionKey = new byte[16];
				System.arraycopy(this.getLMHash(), 0, this.lmUserSessionKey, 0, 8);
				Arrays.fill(this.lmUserSessionKey, 8, 16, (byte) 0);
			}

			return this.lmUserSessionKey;
		}

		public byte[] getNTLMUserSessionKey() throws NTLMEngineException {
			if (this.ntlmUserSessionKey == null) {
				final NTLMEngineImpl.MD4 md4 = new NTLMEngineImpl.MD4();
				md4.update(this.getNTLMHash());
				this.ntlmUserSessionKey = md4.getOutput();
			}

			return this.ntlmUserSessionKey;
		}

		public byte[] getNTLMv2UserSessionKey() throws NTLMEngineException {
			if (this.ntlmv2UserSessionKey == null) {
				final byte[] ntlmv2hash = this.getNTLMv2Hash();
				final byte[] truncatedResponse = new byte[16];
				System.arraycopy(this.getNTLMv2Response(), 0, truncatedResponse, 0, 16);
				this.ntlmv2UserSessionKey = NTLMEngineImpl.hmacMD5(truncatedResponse, ntlmv2hash);
			}

			return this.ntlmv2UserSessionKey;
		}

		public byte[] getNTLM2SessionResponseUserSessionKey() throws NTLMEngineException {
			if (this.ntlm2SessionResponseUserSessionKey == null) {
				final byte[] ntlm2SessionResponseNonce = this.getLM2SessionResponse();
				final byte[] sessionNonce = new byte[this.challenge.length + ntlm2SessionResponseNonce.length];
				System.arraycopy(this.challenge, 0, sessionNonce, 0, this.challenge.length);
				System.arraycopy(ntlm2SessionResponseNonce, 0, sessionNonce, this.challenge.length, ntlm2SessionResponseNonce.length);
				this.ntlm2SessionResponseUserSessionKey = NTLMEngineImpl.hmacMD5(sessionNonce, this.getNTLMUserSessionKey());
			}

			return this.ntlm2SessionResponseUserSessionKey;
		}

		public byte[] getLanManagerSessionKey() throws NTLMEngineException {
			if (this.lanManagerSessionKey == null) {
				try {
					final byte[] keyBytes = new byte[14];
					System.arraycopy(this.getLMHash(), 0, keyBytes, 0, 8);
					Arrays.fill(keyBytes, 8, keyBytes.length, (byte) -67);
					final Key lowKey = NTLMEngineImpl.createDESKey(keyBytes, 0);
					final Key highKey = NTLMEngineImpl.createDESKey(keyBytes, 7);
					final byte[] truncatedResponse = new byte[8];
					System.arraycopy(this.getLMResponse(), 0, truncatedResponse, 0, truncatedResponse.length);
					Cipher des = Cipher.getInstance("DES/ECB/NoPadding");
					des.init(1, lowKey);
					final byte[] lowPart = des.doFinal(truncatedResponse);
					des = Cipher.getInstance("DES/ECB/NoPadding");
					des.init(1, highKey);
					final byte[] highPart = des.doFinal(truncatedResponse);
					this.lanManagerSessionKey = new byte[16];
					System.arraycopy(lowPart, 0, this.lanManagerSessionKey, 0, lowPart.length);
					System.arraycopy(highPart, 0, this.lanManagerSessionKey, lowPart.length, highPart.length);
				} catch (final Exception var8) {
					throw new NTLMEngineException(var8.getMessage(), var8);
				}
			}

			return this.lanManagerSessionKey;
		}
	}
}
