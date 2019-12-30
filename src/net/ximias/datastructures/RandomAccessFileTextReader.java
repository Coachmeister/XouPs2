package net.ximias.datastructures;

import net.ximias.logging.FileLogAppender;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.Comparator;

public class RandomAccessFileTextReader {
	private final RandomAccessFile file;
	private long charPos;
	private final Charset charset;
	
	public RandomAccessFileTextReader(RandomAccessFile file, Charset charset) {
		this.file = file;
		this.charset = charset;
	}
	
	/**
	 * Used to get the end position of the file.
	 *
	 * @return the end position of the file.
	 */
	public long endOfFile() throws IOException {
		synchronized (file) {
			return file.length() / 2;
		}
	}
	
	/**
	 * Use a comparator to locate a line with a binary search and sets the position before it.
	 *
	 * @param line               the target to find.
	 * @param locationComparator the comparator to use for determining the location of the lines in relation to each other.
	 * @return {@code true}, if the line exists in the file.
	 */
	public boolean binarySearchLine(String line, Comparator<String> locationComparator, Comparator<String> valueComparator) throws IOException {
		long largeIndex = endOfFile(); // EOF.
		long smallIndex = 0;
		this.charPos = endOfFile() / 2; // Middle of file.
		String readLine;
		while (largeIndex - smallIndex > 2) {
			long currentPos = ((largeIndex - smallIndex) / 2) + smallIndex;
			this.charPos = currentPos;
			readLine = readNextLine();
			int comparison = locationComparator.compare(line, readLine);
			if (comparison == 0) {
				skipLines(-1);
				break;
			} else if (comparison < 0) {
				largeIndex = currentPos;
			} else {
				smallIndex = currentPos;
			}
		}
		for (long i = smallIndex; i < largeIndex; i++) {
			this.charPos = i;
			if (valueComparator.compare(line, readNextLine()) == 0) {
				skipLines(-1);
				//System.out.println(readPreviousLine());
				return true;
			}
		}
		return false;
	}
	
	public boolean binarySearchLine(String line, Comparator<String> comparator) throws IOException {
		return binarySearchLine(line, comparator, String::compareTo);
	}
	
	/**
	 * Used to set the file read location.
	 *
	 * @param location the location in the file to start reading from
	 */
	public void seek(long location) throws IOException {
		charPos = location;
		if (isOutOfBounds()) throw new IndexOutOfBoundsException("Provided value is out of bounds: " + location + " size: " + endOfFile());
	}
	
	
	/**
	 * Skips a number of lines forwards and backwards in the file.
	 *
	 * @param lines the amount of lines to skip. May be negative.
	 */
	public void skipLines(int lines) throws IOException {
		if (lines == 0) return;
		if (lines < 0) {
			findPrevLineBreak();
			charPos--;
			if (charPos <= 0) {
				charPos = 0;
				return;
			}
			skipLines(lines + 1);
		} else {
			findNextLineBreak();
			charPos++;
			if (charPos >= endOfFile()) {
				charPos = endOfFile();
				return;
			}
			skipLines(lines - 1);
		}
		
	}
	
	/**
	 * Finds the target line, and places the cursor before it.
	 *
	 * @param line the target to locate.
	 * @return true, if the line is contained within the file.
	 * @see #binarySearchLine(String, Comparator)
	 */
	public boolean seekLine(String line) throws IOException {
		charPos = 0;
		while (hasNext()) {
			String readLine = readNextLine();
			if (readLine.equals(line)) break;
		}
		String pastLine = readPreviousLine();
		return pastLine != null && pastLine.equals(line);
	}
	
	/**
	 * Reads the next line from the file, and returns it.
	 *
	 * @return the next line.
	 */
	public String readNextLine() throws IOException {
		if (!hasNext()) {
			//System.out.println("No more lines found");
			return null;
		}
		if (charPos != 0) {
			findNextLineBreak();
			charPos++;
		}
		long lineStart = charPos;
		charPos++;
		findNextLineBreak();
		long lineEnd = charPos;
		charPos = lineStart;
		
		//System.out.println("Line start: " + lineStart);
		//System.out.println("Line end: " + lineEnd);
		return readDecodedString((int) (lineEnd - lineStart));
	}
	
	/**
	 * Used to determine if more lines are available in the forward direction.
	 *
	 * @return {@code true}, if there are more lines to be read.
	 * @throws IOException
	 */
	public boolean hasNext() throws IOException {
		return (charPos <= endOfFile() - 1);
	}
	
	/**
	 * Used to obtain the previous line.
	 * readNextLine().equals(readPreviousLine()) will return true as long as hasNext is.
	 *
	 * @return
	 * @throws IOException
	 */
	public String readPreviousLine() throws IOException {
		if (!hasPrevious()) {
			//System.out.println("No more lines found");
			return null;
		}
		if (charPos != endOfFile() - 1) {
			findNextLineBreak();
		}
		long lineStart = charPos;
		charPos--;
		findPrevLineBreak();
		long lineEnd = charPos;
		charPos = lineStart;
		
		//System.out.println("Line start: " + lineStart);
		//System.out.println("Line end: " + lineEnd);
		String result = readDecodedString((int) (lineEnd - lineStart));
		charPos = Math.max(lineEnd - 1, 0);
		return result;
	}
	
	/**
	 * Used to determine if there are more lines in the backwards direction.
	 *
	 * @return {@code true}, if there are more lines to be read in the backwards direction.
	 */
	public boolean hasPrevious() {
		return (charPos >= 1);
	}
	
	/**
	 * Reads a decoded string from the current character position and length forwards or backwards.
	 *
	 * @param length the length of data to read. May be negative.
	 * @return the read data.
	 * @throws IOException
	 */
	private String readDecodedString(int length) throws IOException {
		StringBuilder sb = new StringBuilder();
		MappedByteBuffer bb = performReadOnlyRead((length));
		if (bb == null) {
			return null;
		}
		CharBuffer decode = charset.decode(bb);
		while (decode.hasRemaining()) {
			char c = decode.get();
			// GO AWAY, ZERO-WIDTH-NO-BREAK-SPACE. YOU'RE NOT REALLY THERE!:
			if (Character.getType(c) != Character.FORMAT && Character.getType(c) != Character.CONTROL) {
				sb.append(c);
			}
		}
		return sb.toString();
	}
	
	/**
	 * convenience method for locating the next line break.
	 *
	 * @throws IOException
	 */
	private void findNextLineBreak() throws IOException {
		findLineBreak(true);
	}
	
	/**
	 * convenience method for locating the previous line break.
	 *
	 * @throws IOException
	 */
	private void findPrevLineBreak() throws IOException {
		if (charPos == endOfFile()) charPos--;
		findLineBreak(false);
	}
	
	/**
	 * Locates the next line break in either direction.
	 *
	 * @param positive the direction to locate the line break in.
	 * @throws IOException
	 */
	private void findLineBreak(boolean positive) throws IOException {
		if (charPos != 0 || charPos < endOfFile()) {
			
			while (charPos < endOfFile()) {
				
				MappedByteBuffer map = performReadOnlyRead(positive ? 1 : -1);
				if (map == null) {
					//System.out.println("read produced null.");
					return;
				}
				
				//Decode
				CharBuffer decode = FileLogAppender.UTF16.decode(map);
				boolean lineBreak = false;
				while (decode.hasRemaining()) {
					char character = decode.get();
					//System.out.println(Character.getName(character));
					
					if (character == '\n' || lineBreak) {
						lineBreak = true;
						if (positive) charPos--;
						else charPos++;
					}
				}
				if (lineBreak) {
					return;
				}
			}
			//System.out.println("Reached EOF.");
		}
	}
	
	/**
	 * Reads the individual bytes from the file.
	 *
	 * @param length the amount of bytes to read. May be negative.
	 * @return a buffer containing the read bytes.
	 * @throws IOException
	 */
	private MappedByteBuffer performReadOnlyRead(int length) throws IOException {
		final MappedByteBuffer result;
		if (isOutOfBounds()) throw new Error("End index out of bounds");
		
		long endCharPosition = charPos + length;
		if (endCharPosition > endOfFile()) {
			//System.out.println("Too large");
			return performReadOnlyRead((int) (endOfFile() - charPos));
		}
		if (endCharPosition < 0) {
			if (charPos <= 0) return null;
			//System.out.println("Too small: "+charPos + " : "+length);
			return performReadOnlyRead((int) -charPos);
		}
		
		synchronized (file) {
			if (length < 0) {
				length = Math.abs(length);
				charPos -= length;
				result = file.getChannel().map(FileChannel.MapMode.READ_ONLY, charPos * 2, length * 2);
			} else {
				result = file.getChannel().map(FileChannel.MapMode.READ_ONLY, charPos * 2, length * 2);
				charPos += length;
			}
		}
		return result;
	}
	
	/**
	 * used to determine if the character position is out of bounds.
	 *
	 * @return {@true}, if the character position is out of bounds.
	 * @throws IOException
	 */
	private boolean isOutOfBounds() throws IOException {
		return charPos < 0 && charPos > endOfFile();
	}
}
