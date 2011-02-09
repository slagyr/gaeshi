package gaeshi.util;

import org.junit.Test;
import static junit.framework.Assert.*;

/*
	 RFC 2045 - Multipurpose Internet Mail Extensions (MIME) Part One: Format of Internet Message Bodies
	 section 6.8.  Base64 Content-Transfer-Encoding
   The encoding process represents 24-bit groups of input bits as output
   strings of 4 encoded characters.  Proceeding from left to right, a
   24-bit input group is formed by concatenating 3 8bit input groups.
   These 24 bits are then treated as 4 concatenated 6-bit groups, each
   of which is translated into a single digit in the base64 alphabet.
   When encoding a bit stream via the base64 encoding, the bit stream
   must be presumed to be ordered with the most-significant-bit first.
   That is, the first bit in the stream will be the high-order bit in
   the first 8bit byte, and the eighth bit will be the low-order bit in
   the first 8bit byte, and so on.
*/

public class Base64Test
{
  @Test
  public void getValueFor() throws Exception
  {
    assertEquals(0, Base64.getValueFor((byte) 'A'));
    assertEquals(26, Base64.getValueFor((byte) 'a'));
    assertEquals(52, Base64.getValueFor((byte) '0'));
  }

  @Test
  public void DecodeNothing() throws Exception
  {
    assertEquals("", Base64.decode(""));
  }

  @Test
  public void DecodeOneChar() throws Exception
  {
    assertEquals("a", Base64.decode("YQ=="));
  }

  @Test
  public void DecodeTwoChars() throws Exception
  {
    assertEquals("a:", Base64.decode("YTo="));
  }

  @Test
  public void DecodeLongSample() throws Exception
  {
    assertEquals("Aladdin:open sesame", Base64.decode("QWxhZGRpbjpvcGVuIHNlc2FtZQ=="));
  }

  @Test
  public void EncodeNothing() throws Exception
  {
    assertEquals("", Base64.encode(""));
  }

  @Test
  public void EncodeOneChar() throws Exception
  {
    assertEquals("YQ==", Base64.encode("a"));
  }

  @Test
  public void EncodeTwoChars() throws Exception
  {
    assertEquals("YTo=", Base64.encode("a:"));
  }

  @Test
  public void EncodeThreeChars() throws Exception
  {
    assertEquals("YWJj", Base64.encode("abc"));
  }

  @Test
  public void EncodeLongSample() throws Exception
  {
    assertEquals("QWxhZGRpbjpvcGVuIHNlc2FtZQ==", Base64.encode("Aladdin:open sesame"));
  }

  @Test
  public void EncodeNuls() throws Exception
  {
    assertEquals("AAAA", Base64.encode("\0\0\0"));
    assertEquals("AAA=", Base64.encode("\0\0"));
    assertEquals("AA==", Base64.encode("\0"));
  }

  @Test
  public void EncodeBinary() throws Exception
  {
    assertEquals("////", new String(Base64.encode(new byte[]{-1, -1, -1})));
    assertEquals("WqVapVql", new String(Base64.encode(new byte[]{90, -91, 90, -91, 90, -91})));
  }

  @Test
  public void DecodeNuls() throws Exception
  {
    assertEquals(Base64.decode("AAAA"), "\0\0\0");
    assertEquals(Base64.decode("AAA="), "\0\0");
    assertEquals(Base64.decode("AA=="), "\0");
  }

  @Test
  public void DecodeBinary() throws Exception
  {
    assertEquals(Base64.decode("////"), new String(new byte[]{-1, -1, -1}));
    assertEquals(Base64.decode("WqVapVql"), new String(new byte[]{90, -91, 90, -91, 90, -91}));
  }

}
