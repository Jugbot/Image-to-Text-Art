import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

public class MainImageToText {

	public static void main(String[] args) {
		CharDictionary cd = new CharDictionary(new Font("Times New Roman", 1, 20));
		JFileChooser chooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("PNG Images", "png");
		chooser.setFileFilter(filter);
		chooser.setCurrentDirectory(new File(System.getProperty("user.home"), "Desktop"));
		int returnVal = chooser.showOpenDialog(null);
		File f = chooser.getSelectedFile();
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			System.out.println("\nYou chose to open this file: " + f);

			File output = new File(f.getParentFile(), "$output.txt");
			saveTextFile(output, ImageToText(f, cd));
		}
	}

	static String ImageToText(File f, CharDictionary cd) {
		BufferedImage img = null;
		String text = "";
		try {
			img = ImageIO.read(f);
		} catch (IOException e) {
			e.printStackTrace();
		}

		int w = img.getWidth();
		int h = img.getHeight();

		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				Color col = new Color(img.getRGB(x, y));
				int red = col.getRed();
				int green = col.getGreen();
				int blue = col.getBlue();
				int finalAverage = (red + green + blue) / 3;
				text += cd.getDictionaryLetter(finalAverage);
				System.out.println(x + ", " + y);
			}
			text += "\n";
		}
		cd.debugPrintDictionary();
		// System.out.println(text);
		return text;
	}

	static void saveTextFile(File f, String s) {

		File desktop = new File(System.getProperty("user.home"), "Desktop");
		File outputfile = new File(desktop.toString() + "\\" + "file.txt");
		try {
			System.out.println("creating file...");
			PrintWriter out = new PrintWriter(outputfile, "UTF-8");
			out.write(s);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Saved to: " + f);
	}
}

class letter {
	public char c;
	public double val;

	letter(char ch, double dub) {
		c = ch;
		val = dub;
	}
}

class LetterComparator implements Comparator<letter> {
	@Override
	public int compare(letter a, letter b) {
		return (int) (a.val*100 - b.val*100); // *100 for more precision
	}
}

class CharDictionary {
	private Font f;
	private List<letter> dictionary = new ArrayList<letter>();

	CharDictionary(Font font) {
		f = font;
		createDictionary();
	}

	public void createDictionary() {
		for (int i = 33; i <= 126; i++) { //33 126

			dictionary.add(new letter((char) i, getValue((char) i)));
		}
		sort();
	}

	public void sort() {
		Collections.sort(dictionary, new LetterComparator());
	}

	public void debugPrintDictionary() {
		for (int i = 0; i < dictionary.size(); i++) {
			System.out.println("Char: " + dictionary.get(i).c + " Valueeeee: " + dictionary.get(i).val);
		}
	}

	public char getDictionaryLetter(int val) {
		for (int i = 0; i < dictionary.size(); i++) {
			double charvalue = dictionary.get(i).val / dictionary.get(dictionary.size() - 1).val * 255;
			if (charvalue >= 255-val) {//inverted here

				return dictionary.get(i).c;
			}
		}

		return dictionary.get(dictionary.size() - 1).c; /// PROBLEM HERE
	}

	public double getValue(char c) {
		BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
		Color fntC = new Color(255, 255, 255);
		Graphics2D g = img.createGraphics();

		g.setFont(f);

		FontMetrics fm = g.getFontMetrics();

		int width = fm.charWidth(c);
		int height = fm.getAscent(); // too big

		img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		g = img.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(fntC);
		g.setFont(f);

		// g.fill
		g.drawString(c + "", 0, (fm.getAscent() + (height - (fm.getAscent() + fm.getDescent())) / 2));

		int w = img.getWidth();
		int h = img.getHeight();
		double finalAverage = 0;

		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				Color col = new Color(img.getRGB(x, y));
				int red = col.getRed();
				int green = col.getGreen();
				int blue = col.getBlue();
				finalAverage += (red + green + blue) / 3;

			}
		}

		finalAverage /= w * h;

		try {
			File desktop = new File(System.getProperty("user.home"), "Desktop");
			File outputfile = new File(desktop.toString() + "\\letters\\" + (int) c + ".png");
			ImageIO.write(img, "png", outputfile);
		} catch (IOException e) {

		}

		return finalAverage;
	}
}