# Empressia Picocog Extension

## 目次

* [概要](#概要)
* [追加されている機能](#追加されている機能)
* [使い方](#使い方)
* [ライセンス](#ライセンス)
* [使用しているライブラリ](#使用しているライブラリ)

## 概要

Empressia製のPicocog拡張です。  
文字列出力だけだとつらいので、できるだけシンプルで直感的な置換機能を追加しています。  

## 追加されている機能

* 置換機能が追加されています。

	追加されているメソッドのSuffixは_n、_o、_c、_mの4種類です。  
	_n……オリジナルのPicoWriterのSuffixがないものと同じ感じ。  
	_o……オリジナルのPicoWriterのSuffixが_rのものと同じ感じ。  
	_c……オリジナルのPicoWriterのSuffixが_lのものと同じ感じ。  
	_m……オリジナルのPicoWriterのSuffixが_lrのものと同じ感じ。  
	いずれのメソッドにも、可変長引数版が追加されていて、置換する機能が追加されています。  

* インデントを切り替えられます。初期値はタブになります。

* インデントのみの行にはインデントがつかなくなっています。

## 使い方

### 依存関係にライブラリを追加します。  

```groovy
dependencies {
	implementation(group:"jp.empressia", name:"jp.empressia.picocog", version:"1.0.0");
}
```

※上記のサンプルのバージョンは、最新ではない可能性があります。  

### 使用サンプル

```java
EPicoWriter writer = new EPicoWriter();
// 第2引数以降の値で、第1引数のマークの場所を置き換えて出力します。
writer.writeln_n("package {0};", "jp.empressia.picocog.writer");
writer.writeln_n("");
writer.writeln_o("public class {0} extends {1} {", "EPicoWriter", "PicoWriter");
EPicoWriter methodWriter = writer.createDeferredEPicoWriter();
writer.writeln_c("}");

methodWriter.writeln_n("public void somethingMethod() {}");
methodWriter.writeln_n("	return;");
methodWriter.writeln_n("}");

String generated = writer.toString();
System.out.println(generated);
```

出力結果：
```java
package jp.empressia.picocog.writer;

public class EPicoWriter extends PicoWriter {

	public void somethingMethod() {
		return;
	}

}
```

## ライセンス

いつも通りのライセンスです。  
zlibライセンス、MITライセンスでも利用できます。  

ただし、チーム（複数人）で使用する場合は、MITライセンスとしてください。  

## 使用しているライブラリ

* Picocog
	* https://github.com/ainslec/picocog

## 注意

プロジェクトはVSCodeのJava拡張機能ではテストを実行できないようです（2021/05/01）。  
Gradleからは実行できます。  
