package be.thomaswinters.corpora.com.textfiles;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 * Simple script to download all text files of indexes of textfiles.com
 * 
 * @author Thomas Winters
 *
 */
public class TextFilesDownloader {

	private final File downloadLocation;

	public TextFilesDownloader(File downloadLocation) {
		this.downloadLocation = downloadLocation;
	}

	public TextFilesDownloader() {
		this(new File(".\\textfiles-com\\"));
	}

	public void download(String parent, String url) throws MalformedURLException, IOException {
		if (url.length() > 0 && url.endsWith(".txt") || url.endsWith(".doc")) {
			downloadFile(parent, url);
		} else {
			downloadDirectIndex(parent, url);
		}
	}

	private String toFullUrl(String parent, String url) {
		return (parent.length() > 0 ? parent + (parent.endsWith("/") ? "" : "/") : "") + url;

	}

	private void downloadDirectIndex(String parent, String file) throws IOException {
		String fullUrl = toFullUrl(parent, file);
		System.out.println("Searching " + fullUrl);
		Document doc;
		try {
			doc = Jsoup.connect(fullUrl).get();
		} catch (UnsupportedMimeTypeException e) {
			System.out.println("Not supported mimetype: " + e.getMessage() + ". Downloading as file.");
			downloadFile(parent, file);
			return;
		}
		Elements possibilities = doc.select(" a");

		if (possibilities.isEmpty()) {
			System.out.println("Nope, downloading as txt instead: " + parent + " -> " + file);
			downloadFile(parent, file);
			return;
		}
		possibilities.stream().map(e -> e.attr("href")).forEach(e -> {
			try {
				download(fullUrl, e);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		});
	}

	private void downloadFile(String parent, String file) throws MalformedURLException, IOException {
		try {
			FileUtils.copyURLToFile(new URL(toFullUrl(parent, file)), new File(downloadLocation, file));
		} catch (FileNotFoundException e) {
			System.out.println("Didn't find file: " + e);
			// Nothing
		}
	}

	public static void main(String[] args) throws IOException {
		if (args.length <= 0) {
			throw new IllegalArgumentException("Please provide a url as an argument, e.g. 'http://textfiles.com/humor/JOKES/'");
		}
		new TextFilesDownloader().download("", args[0]);
	}
}
