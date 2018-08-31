package net.ximias.datastructures;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;

class RandomAccessFileTextReaderTest {
	private static final File fileName = new File("files/randomAccessReaderTest");
	private final RandomAccessFile file = new RandomAccessFile(fileName,"rw");
	private final Charset charset = Charset.forName("utf16");
	private final RandomAccessFileTextReader reader = new RandomAccessFileTextReader(file, charset);
	private static String lastLine = "9";
	private static String firstLine;
	private static boolean isSetup = true;
	
	RandomAccessFileTextReaderTest() throws IOException {
	}

	@SuppressWarnings("ResultOfMethodCallIgnored") // File stuff returns a boolean.
	@BeforeAll
	static void setUpAll() {
		fileName.getParentFile().mkdirs();
		fileName.delete();
		try {
			fileName.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void finalize() throws Throwable {
		file.close();
	}
	
	@BeforeEach
	void setUp() throws IOException {
		if (isSetup){
			isSetup = false;
			for (int i = 100; i <= 300; i++) {
				writeToFile(nextLine());
			}
		}
		file.seek(0);
		reader.seek(0);
	}
	
	private void writeToFile(String i) throws IOException {
		if (i.isEmpty()) return;
		synchronized (file){
			String toWrite;
			if (firstLine == null) {
				firstLine = i;
				toWrite = i;
			}else{
				toWrite = '\n'+i;
			}
			lastLine= i;
			file.seek(file.length());
			ByteBuffer buf = charset.encode(toWrite);
			while (buf.hasRemaining()){
				byte b = buf.get();
				file.write(b);
			}
		}
	}
	
	@Test
	void endOfFile() throws IOException {
		assertEquals(file.length()/2,reader.endOfFile());
		writeToFile(nextLine());
		assertEquals(file.length()/2,reader.endOfFile());
	}
	
	@Test
	void binarySearchLine() throws IOException {
		assertTrue(reader.binarySearchLine(lastLine,String::compareTo),"highest possible value should be findable");
		assertTrue(reader.binarySearchLine(firstLine,String::compareTo), "Lowest possible value should be findable.");
		assertTrue(reader.binarySearchLine(lastLine,String::compareTo), "High value should be findable after any other lookup.");
		assertFalse(reader.binarySearchLine("9",String::compareTo), "Not in file.");
		String nextLine = nextLine();
		assertFalse(reader.binarySearchLine(nextLine,String::compareTo), "Not in file.");
		writeToFile(nextLine);
		assertTrue(reader.binarySearchLine(nextLine,String::compareTo), "Just added.");
		assertEquals(nextLine, reader.readNextLine());
		assertTrue(reader.binarySearchLine("202",String::compareTo));
	}
	
	@Test
	void binarySearchLine1() throws IOException {
		assertTrue(reader.binarySearchLine(lastLine,Comparator.comparing(s -> s.substring(0,1)),String::compareTo),"highest possible value should be findable");
		assertTrue(reader.binarySearchLine(firstLine,Comparator.comparing(s -> s.substring(0,1)),String::compareTo), "Lowest possible value should be findable.");
		assertTrue(reader.binarySearchLine(lastLine,Comparator.comparing(s -> s.substring(0,1)),String::compareTo), "High value should be findable after any other lookup.");
		assertFalse(reader.binarySearchLine("09",Comparator.comparing(s -> s.substring(0,1)),String::compareTo), "Not in file.");
		String nextLine = nextLine();
		assertFalse(reader.binarySearchLine(nextLine,Comparator.comparing(s -> s.substring(0,1)),String::compareTo), "Not in file.");
		writeToFile(nextLine);
		assertTrue(reader.binarySearchLine(nextLine,Comparator.comparing(s -> s.substring(0,1)),String::compareTo), "Just added.");
	}
	
	@Test
	void seek() throws IOException {
		reader.seek(0);
		String firstRead = reader.readNextLine();
		reader.seek(0);
		assertEquals(firstRead,reader.readNextLine(), "Skipping to 0 should give same result.");
		
		reader.seek(reader.endOfFile());
		String lastRead = reader.readPreviousLine();
		reader.seek(reader.endOfFile());
		assertEquals(lastRead, reader.readPreviousLine(),"Skipping to last should give same result");
	}
	
	@Test
	void skipLines() throws IOException {
		String firstRead = reader.readNextLine();
		reader.skipLines(-1);
		assertEquals(firstRead, reader.readNextLine(),"Skipping backwards should yield the same line");
		
		reader.skipLines(-1);
		reader.skipLines(1);
		assertNotEquals(firstRead, reader.readNextLine(),"Skips one too many backwards.");
		
		reader.seek(reader.endOfFile());
		assertNull(reader.readNextLine());
		reader.skipLines(-1);
		assertEquals(lastLine, reader.readNextLine(),"Skipping 1 line from EOF did not skip any lines");
	}
	
	@Test
	void seekLine() throws IOException {
		assertTrue(reader.seekLine(lastLine),"highest possible value should be findable");
		assertTrue(reader.seekLine(firstLine), "Lowest possible value should be findable.");
		assertTrue(reader.seekLine(lastLine), "High value should be findable after any other lookup.");
		assertFalse(reader.seekLine("9"), "Not in file.");
		String nextLine = nextLine();
		assertFalse(reader.seekLine(nextLine), "Not in file.");
		writeToFile(nextLine);
		assertTrue(reader.seekLine(nextLine), "Just added.");
	}
	
	private String nextLine() {
		return String.valueOf((Integer.valueOf(lastLine) + 1));
	}
	
	@Test
	void readNextLine() throws IOException {
		String firstRead = reader.readNextLine();
		assertEquals(firstLine, firstRead,"First read should yield 10");
		assertNotEquals(firstRead, reader.readNextLine(), "Successive reads should read new lines.");
		reader.seek(reader.endOfFile());
		assertNull(reader.readNextLine(), "reading with no data should yield null");
	}
	
	@Test
	void hasNext() throws IOException {
		reader.seek(0);
		assertTrue(reader.hasNext(), "Reader should have values available at initial position.");
		reader.seek(reader.endOfFile());
		assertFalse(reader.hasNext(), "Reader should not have available values at end of file.");
	}
	
	@Test
	void readPreviousLine() throws IOException {
		assertNull(reader.readPreviousLine(), "reading without available data should produce null.");
		reader.seek(reader.endOfFile());
		String firstRead = reader.readPreviousLine();
		assertEquals(lastLine,firstRead, "Prev read at EOF should give last added line");
		assertNotEquals(firstRead, reader.readPreviousLine(), "Successive reads should produce different lines.");
	}
	
	@Test
	void hasPrevious() throws IOException {
		reader.seek(0);
		assertFalse(reader.hasPrevious(), "Reader should NOT have available values at initial position.");
		reader.seek(reader.endOfFile());
		assertTrue(reader.hasPrevious(), "Reader SHOULD have available values at end of file.");
	}
	
	@Test
	void testNextPreviousProducesSameValue() throws IOException {
		String firstRead = reader.readNextLine();
		assertEquals(firstRead, reader.readPreviousLine(),"Re-reading a line backwards should yield same result.");
		assertEquals(firstRead, reader.readNextLine(),"Re-reading forwards after reading backwards should yield same result.");
		reader.skipLines(1);
		assertEquals(reader.readPreviousLine(), reader.readNextLine(),"Re-reading a line forwards should yield same result.");
	}
}