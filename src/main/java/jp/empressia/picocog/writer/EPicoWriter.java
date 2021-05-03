package jp.empressia.picocog.writer;

import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ainslec.picocog.IndentedLine;
import org.ainslec.picocog.PicoWriter;
import org.ainslec.picocog.PicoWriterItem;

/**
 * Empressia製のPicoWriter拡張です。
 * 
 * テンプレートというほどではないけど、簡単な置換機能を付与します。
 * 
 * 基本は、MessageFormatのような置換ですが、『{』と『}』は自動でエスケープされます。
 * 文字列以外の置換には、原則、対応していません。つまり、『{0,number,0}』と知った表現は、置換対象ではなくそのままの文字列で出力されます。
 * 
 * 自動的にエスケープされることから、『{0}』のような文字列を出力したい場合は、置換対象になるので、注意してください。
 * このWriterではそのような文字列を出力することはレアケースだと考えています。出力できないわけではないです。
 * 
 * 一部、PicoWriterの実装に依存している部分があり、バージョンによっては、動作しない可能性があります。
 * 
 * @author すふぃあ
 */
public class EPicoWriter extends PicoWriter {

	private Field IndentCountField;
	private Field IndentTextField;
	private Field ContentsField;

	/** コンストラクタ。 */
	public EPicoWriter() {
		this("\t");
	}
	/** コンストラクタ。 */
	public EPicoWriter(String indentText) {
		super(indentText);
		try {
			Field indentCountField = PicoWriter.class.getDeclaredField("_indents");
			indentCountField.setAccessible(true);
			this.IndentCountField = indentCountField;
			Field indentTextField = PicoWriter.class.getDeclaredField("_ic");
			indentTextField.setAccessible(true);
			this.IndentTextField = indentTextField;
			Field contentsField = PicoWriter.class.getDeclaredField("_content");
			contentsField.setAccessible(true);
			this.ContentsField = contentsField;
		} catch(ReflectiveOperationException ex) {
			throw new IllegalStateException("黒魔術（リフレクション）に失敗しました。PicoWriterのバージョンを確認してください。", ex);
		}
	}
	/**
	 * 遅延書き込み用のWriterを作成します。
	 * @param <T> 原則、自身のクラスとなります。
	 * @param c 原則、自身のクラスを指定します。
	 */
	protected <T extends PicoWriter> T createInnerWriter(Class<T> c) {
		T innerWriter;
		try {
			innerWriter = c.getConstructor().newInstance();
			Field indentCountField = this.IndentCountField;
			int initialIndent = indentCountField.getInt(this);
			indentCountField.setInt(innerWriter, initialIndent);
			Field indentTextField = this.IndentTextField;
			String indentText = (String)indentTextField.get(this);
			indentTextField.set(innerWriter, indentText);
			this.writeln(innerWriter);
		} catch(ReflectiveOperationException ex) {
			throw new IllegalStateException("黒魔術（リフレクション）に失敗しました。PicoWriterのバージョンを確認してください。", ex);
		}
		return innerWriter;
	}
	/** 遅延書き込み用のWriterを作成します。 */
	public EPicoWriter createDeferredEPicoWriter() {
		return this.createInnerWriter(this.getClass());
	}
	/** 書き込みます。 */
	public void writeln_n(String string) {
		this.writeln(string);
	}
	/** 書き込みます。 */
	public void writeln_n(String template, Object... args) {
		String escapedTemplate = escapeTemplate(template);
		String string = MessageFormat.format(escapedTemplate, args);
		this.writeln(string);
	}
	/** 書き込んで、次の行でインデントを加えます。 */
	public void writeln_o(String string) {
		this.writeln_r(string);
	}
	/** 書き込んで、次の行でインデントを加えます。 */
	public void writeln_o(String template, Object... args) {
		String escapedTemplate = escapeTemplate(template);
		String string = MessageFormat.format(escapedTemplate, args);
		this.writeln_r(string);
	}
	/** この行でインデントを戻した状態で、書き込みます。 */
	public void writeln_c(String string) {
		this.writeln_l(string);
	}
	/** この行でインデントを戻した状態で、書き込みます。 */
	public void writeln_c(String template, Object... args) {
		String escapedTemplate = escapeTemplate(template);
		String string = MessageFormat.format(escapedTemplate, args);
		this.writeln_l(string);
	}
	/** この行でインデントを戻した状態で、書き込んで次の行でインデントを加えます。 */
	public void writeln_m(String string) {
		this.writeln_lr(string);
	}
	/** この行でインデントを戻した状態で、書き込んで次の行でインデントを加えます。 */
	public void writeln_m(String template, Object... args) {
		String escapedTemplate = escapeTemplate(template);
		String string = MessageFormat.format(escapedTemplate, args);
		this.writeln_lr(string);
	}
	private static String escapeTemplate(String template) {
		Pattern placeHolderRegex = Pattern.compile("\\{[+-]?[0-9]+\\}");
		String escapedTemplate;
		if(template.indexOf("{") != -1) {
			// {や}は自動でエスケープする＆エスケープしない前提（だから置換目的以外と思うのは全部エスケープして良い）。
			// 書式は非対応（エスケープされる）。
			// １．『{』を探す。
			// ２．『{数値}』か確認する。
			// ３．＃２に一致した部分以外はエスケープする。
			StringBuilder escaped = new StringBuilder();
			int appendIndex = 0;
			Matcher m = placeHolderRegex.matcher(template);
			while(m.find(appendIndex)) {
				int startIndex = m.start();
				int endIndex = m.end();
				String r = template.substring(appendIndex, startIndex);
				escaped.append(r.replaceAll("'", "''").replaceAll("\\{", "'{'").replaceAll("\\}", "'}'"));
				String placeHolder = m.group(0);
				escaped.append(placeHolder);
				appendIndex = endIndex;
			}
			String r = template.substring(appendIndex);
			escaped.append(r.replaceAll("'", "''").replaceAll("\\{", "'{'").replaceAll("\\}", "'}'"));
			escapedTemplate = escaped.toString();
		} else {
			escapedTemplate = template.replaceAll("'", "''").replaceAll("\\{", "'{'").replaceAll("\\}", "'}'");
		}
		return escapedTemplate;
	}

	/**
	 * 書き込まれた結果を得ます。
	 */
	@Override
	public String toString(int indentBase) {
		try {
			@SuppressWarnings("unchecked")
			ArrayList<PicoWriterItem> contents = (ArrayList<PicoWriterItem>)this.ContentsField.get(this);
			for(int i = 0; i < contents.size(); ++i) {
				PicoWriterItem item = contents.get(i);
				if(item instanceof IndentedLine) {
					class ShrinkIndentedLine extends IndentedLine {
						ShrinkIndentedLine(IndentedLine delegate) {
							super(delegate.getLine(), delegate.getIndent());
						}
						public int getIndent() { return (this.getLine().isEmpty() == false) ? super.getIndent() : 0; }
					}
					IndentedLine line = (IndentedLine)item;
					ShrinkIndentedLine sline = new ShrinkIndentedLine(line);
					contents.set(i, sline);
				}
			}
			return super.toString(indentBase);
		} catch(ReflectiveOperationException ex) {
			throw new IllegalStateException("黒魔術（リフレクション）に失敗しました。使用しているPicoWriterのバージョンを確認してください。", ex);
		}
	}

}
