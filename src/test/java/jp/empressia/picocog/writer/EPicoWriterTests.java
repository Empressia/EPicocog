package jp.empressia.picocog.writer;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.Test;

/**
 * EPicoWriterのテスト。
 * @author すふぃあ
 */
public class EPicoWriterTests {

	/**
	 * 適当な出力がエラーにならないで正常終了する。
	 * 今のところ、PicoWrtierの制限で改行コードはLFに固定されている。
	 */
	@Test
	public void 適当な出力がエラーにならないで正常終了する() {
		String expected = "" +
			"package jp.empressia.picocog.writer;\n" +
			"\n" +
			"public class EPicoWriter extends PicoWriter {\n" +
			"\n" +
			"	public void somethingMethod() {\n" +
			"		return;\n" +
			"	}\n" +
			"\n" +
			"}\n";

		EPicoWriter writer = new EPicoWriter();
		// 第2引数以降の値で、第1引数のマークの場所を置き換えて出力します。
		writer.writeln_n("package {0};", "jp.empressia.picocog.writer");
		writer.writeln_n("");
		writer.writeln_o("public class {0} extends {1} {", "EPicoWriter", "PicoWriter");
		writer.writeln_n("");
		EPicoWriter methodWriter = writer.createDeferredEPicoWriter();
		writer.writeln_n("");
		writer.writeln_c("}");

		methodWriter.writeln_n("public void somethingMethod() {");
		methodWriter.writeln_n("	return;");
		methodWriter.writeln_n("}");

		String generated = writer.toString();
		System.out.println(generated);
		assertAll(
			() -> assertThat("期待した出力が得られる。", generated, is(expected))
		);
	}

}
