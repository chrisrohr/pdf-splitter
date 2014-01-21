package com.evanguardsolutions.pdfsplit;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdfwriter.COSWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.Splitter;

public class PDFSplitter {
	public static void main(String[] args) {
		try {
			// Checks the program arguments to make sure the original file has been provided
			if (args.length == 0) {
				System.out.println("Err: Missing original file");
				System.exit(1);
			}
			
			String originalFile = args[0];
			
			InputStream input = null;
			PDDocument document = null;
			BufferedReader br = null;
			
			try {
				// This try block tries to read the original file and parse the PDF document
				try {
					input = new FileInputStream(originalFile);
					document = parseDocument(input);
				} catch (Exception e) {
					System.out.println("Err: Unable to read file [" + originalFile + "]");
					System.exit(1);
				}
				
				// Setting up to read from standard in
				br = new BufferedReader(new InputStreamReader(System.in));
				
				// Reading each line of the standard input.  Will exit when quit is encountered (or the input stream is closed).
				String inLine;
				while (!(inLine=br.readLine()).equals("quit")) {
					Splitter splitter = new Splitter();
					
					// Splitting out the pieces of the read line. Format: start_page end_page filename
					String[] inputSplit = inLine.split(" ");
					
					int startPage = Integer.parseInt(inputSplit[0]);
					int endPage = Integer.parseInt(inputSplit[1]);
					String outFile = inputSplit[2];
					
					// Because this library expects to split a file evenly, we are tricking it by setting the start page, end page, and setting the split = the end page (making one output)
					splitter.setStartPage(startPage);
					splitter.setEndPage(endPage);
					splitter.setSplitAtPage(endPage);
					
					// Split the document and write out the new file.
					try {
						List<PDDocument> documents = splitter.split(document);
						
						if (documents.size() > 0) {
							PDDocument doc = documents.get(0);
							writeDocument(doc, outFile);
							doc.close();
							System.out.println("Ok: " + outFile);
						}
					} catch (Exception e) {
						System.out.println("Err: " + e.getMessage());
					}
				}
			} finally {
				if (input != null) {
					input.close();
				}
				if (document != null) {
					document.close();
				}
				if (br != null) {
					br.close();
				}
			}
		} catch (Exception e) {}
		
		System.out.println("Exiting");
	}
	
	/**
	 * Takes the original file input stream and parses the PDF for manipulating.
	 * @param input The input stream of the original file
	 * @return The parsed PDF
	 * @throws IOException Thrown if there is a problem reading the original PDF
	 */
	private static PDDocument parseDocument(InputStream input) throws IOException {
		PDFParser parser = new PDFParser(input);
		parser.parse();
		return parser.getPDDocument();
	}
	
	/**
	 * Writes the given PDF document to the given filename.
	 * @param doc The PDF to write
	 * @param filename The filename of the PDF that will be written
	 * @throws IOException Thrown if there is a problem writing the PDF
	 * @throws COSVisitorException Thrown if there is a problem with PDF object
	 */
	private static final void writeDocument(PDDocument doc, String filename) throws IOException, COSVisitorException {
		FileOutputStream output = null;
		COSWriter writer = null;
		
		try {
			output = new FileOutputStream(filename);
			writer = new COSWriter(output);
			writer.write(doc);
		} finally {
			if (output != null) {
				output.close();
			}
			
			if (writer != null) {
				writer.close();
			}
		}
	}
}
