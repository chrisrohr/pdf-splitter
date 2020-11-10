package com.evanguardsolutions.pdfsplit;

import org.apache.pdfbox.io.RandomAccessBufferedFileInputStream;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdfwriter.COSWriter;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class PDFSplitter {
	public static void main(String[] args) {
		// Checks the program arguments to make sure the original file has been provided
		if (args.length == 0) {
			System.out.println("Err: NONE Missing original file");
			System.exit(1);
		}

		String originalFile = args[0];

		try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			 PDDocument document = parseDocument(originalFile)) {

			int totalPages = document.getNumberOfPages();

			System.out.println("Ready:");

			// Reading each line of the standard input.  Will exit when quit is encountered (or the input stream is closed).
			String inLine;
			while (!(inLine = br.readLine()).equals("quit")) {
				Splitter splitter = new Splitter();

				// Splitting out the pieces of the read line. Format: start_page end_page filename
				String[] inputSplit = inLine.split(" ");

				int startPage = Integer.parseInt(inputSplit[0]);
				int endPage = Integer.parseInt(inputSplit[1]);
				String outFile = inputSplit[2];

				if (startPage > totalPages) {
					// Outside page range, sending error and continue
					System.out.println("Err: " + outFile + " Start page [" + startPage + "] or End page [" + endPage + "] out of range.  Total [" + totalPages + "]");
					continue;
				}

				if (endPage > totalPages) {
					endPage = totalPages;
				}

				// Because this library expects to split a file evenly, we are tricking it by setting the start page, end page, and setting the split = the end page (making one output)
				splitter.setStartPage(startPage);
				splitter.setEndPage(endPage);
				splitter.setSplitAtPage(endPage);

				// Split the document and write out the new file.
				splitAndWriteNewFile(document, splitter, outFile);
			}
		} catch (Exception e) {
			System.out.println("Err: NONE Unable to read file [" + originalFile + "]");
			System.exit(1);
		}

		System.out.println("Exiting");
	}

	private static void splitAndWriteNewFile(PDDocument document, Splitter splitter, String outFile) {
		try {
			List<PDDocument> documents = splitter.split(document);

			if (!documents.isEmpty()) {
				PDDocument doc = documents.get(0);
				writeDocument(doc, outFile);
				doc.close();
				System.out.println("Ok: " + outFile);
			}
		} catch (Exception e) {
			System.out.println("Err: " + outFile + " " + e.getMessage());
		}
	}
	
	/**
	 * Takes the original file input stream and parses the PDF for manipulating.
	 * @param input The file name of the original file
	 * @return The parsed PDF
	 * @throws IOException Thrown if there is a problem reading the original PDF
	 */
	private static PDDocument parseDocument(String input) throws IOException {
		PDFParser parser = new PDFParser(new RandomAccessBufferedFileInputStream(input));
		parser.parse();
		return parser.getPDDocument();
	}
	
	/**
	 * Writes the given PDF document to the given filename.
	 * @param doc The PDF to write
	 * @param filename The filename of the PDF that will be written
	 * @throws IOException Thrown if there is a problem writing the PDF
	 */
	private static void writeDocument(PDDocument doc, String filename) throws IOException {

		try (FileOutputStream output = new FileOutputStream(filename); COSWriter writer = new COSWriter(output)) {
			writer.write(doc);
		}
	}
}
