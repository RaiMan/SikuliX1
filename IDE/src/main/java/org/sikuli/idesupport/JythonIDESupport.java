/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.idesupport;

import org.sikuli.basics.Debug;
import org.sikuli.script.Image;
import org.sikuli.script.runners.JythonRunner;
import org.sikuli.script.runners.PythonRunner;
import org.sikuli.script.support.generators.ICodeGenerator;
import org.sikuli.script.support.generators.JythonCodeGenerator;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * all methods from/for IDE, that are Python specific
 */
public class JythonIDESupport implements IIDESupport {

	@Override
	public String[] getTypes() {
		return new String[]{JythonRunner.TYPE, PythonRunner.TYPE};
	}

	@Override
	public IIndentationLogic getIndentationLogic() {
		return new PythonIndentation();
	}

	//<editor-fold desc="12 runLines support">
	private static final Pattern[] IF_START_PATTERNS = new Pattern[] { Pattern.compile("\\s*if.*:\\s*") };

	private static final Pattern[] IF_END_PATTERNS = new Pattern[] { Pattern.compile("\\s*elif.*:\\s*"),
					Pattern.compile("\\s*else\\s*:\\s*") };

	private static final Pattern[] TRY_START_PATTERNS = new Pattern[] { Pattern.compile("\\s*try\\s*:\\s*") };

	private static final Pattern[] TRY_END_PATTERNS = new Pattern[] { Pattern.compile("\\s*except.*:\\s*"),
					Pattern.compile("\\s*finally\\s*:\\s*") };

	private static final String GENERATED_MARKER = " # line generated";

	/*
	 * Normalizes a partial script passed to runLines.
	 */
	public String normalizePartialScript(String script) {
		List<String> lines = getLines(script);

		lines = stripComments(lines);
		lines = normalizeIndentation(lines);

		String indentation = detectIndentation(script);
		lines = fixLastLine(lines, indentation);
		lines = fixUnclosedTryBlock(lines, indentation);
		lines = fixUnopenedBlock(lines, TRY_START_PATTERNS, TRY_END_PATTERNS, "try:", indentation);
		lines = fixUnopenedBlock(lines, IF_START_PATTERNS, IF_END_PATTERNS, "if True:", indentation);
		lines = fixFirstLine(lines);

		return String.join("\n", lines) + "\n";
	}

	private static final Pattern COMMENT_PATTERN = Pattern.compile("\\s*#.*");

	private List<String> stripComments(List<String> lines) {
		return lines.stream().filter((line) -> !COMMENT_PATTERN.matcher(line).matches()).collect(Collectors.toList());
	}

	/*
	 * Remove unnecessary indentation.
	 *
	 * In the example it removed the first 2 spaces on each line.
	 *
	 * ---
	 *   if True:
	 *     hello("world") ---
	 */
	private List<String> normalizeIndentation(List<String> lines) {
		while (true) {
			for (String line : lines) {
				if (detectIndentation(line).isEmpty()) {
					return lines;
				}
			}

			lines = lines.stream().map((line) -> line.substring(1)).collect(Collectors.toList());
		}
	}

	/*
	 * Fixes for example the following:
	 *
	 * ---
	 * try:
	 *   print("hello")
	 * ---
	 *
	 * Also handle nested tries and adds the required number of excepts with the
	 * corresponding indentation.
	 *
	 */
	private List<String> fixUnclosedTryBlock(List<String> lines, String indentation) {
		lines = new ArrayList<>(lines);

		List<String> lineIndentations = new LinkedList<>();

		for (String line : lines) {
			if (lineMatches(line, TRY_START_PATTERNS)) {
				lineIndentations.add(detectIndentation(line));
			} else if (lineMatches(line, TRY_END_PATTERNS)) {
				if (!lineIndentations.isEmpty()) {
					String lineIndentation = detectIndentation(line);
					if (lineIndentation.equals(lineIndentations.get(lineIndentations.size() - 1))) {
						lineIndentations.remove(lineIndentations.size() - 1);
					}
				}
			}
		}

		Collections.reverse(lineIndentations);

		for (String lineIndentation : lineIndentations) {
			lines.add(lineIndentation + "except:" + GENERATED_MARKER);
			lines.add(lineIndentation + indentation + "raise" + GENERATED_MARKER);
		}

		return lines;
	}

	/*
	 * Fixes for example the following:
	 *
	 * ---
	 *     print("foo")
	 *   else print("bar")
	 * except:
	 *   print("error") ---
	 *
	 * Creates the required try or if to get a valid block. Also works with nested
	 * blocks.
	 */
	private List<String> fixUnopenedBlock(List<String> lines, Pattern[] startPatterns, Pattern[] endPatterns,
																				String startExpression, String indentation) {
		lines = new ArrayList<>(lines);

		List<Integer> lineNumbers = new ArrayList<>();
		List<String> lineIndentations = new ArrayList<>();

		for (int lineNumber = lines.size() - 1; lineNumber >= 0; lineNumber--) {
			String line = lines.get(lineNumber);
			if (lineMatches(line, endPatterns)) {
				String lineIndentation = detectIndentation(line);
				if (lineIndentations.isEmpty() || !lineIndentation.equals(lineIndentations.get(0))) {
					lineNumbers.add(0, lineNumber);
					lineIndentations.add(0, lineIndentation);
				} else {
					lineNumbers.set(0, lineNumber);
					lineIndentations.set(0, lineIndentation);
				}
			} else if (lineMatches(line, startPatterns)) {
				if (!lineNumbers.isEmpty()) {
					lineNumbers.remove(0);
					lineIndentations.remove(0);
				}
			}
		}

		int insertCount = 0;

		for (int i = lineNumbers.size() - 1; i >= 0; i--) {
			int lineNumber = lineNumbers.get(i) + (insertCount++);
			String lineIndentation = lineIndentations.get(i);

			int index = 0;

			for (int n = lineNumber - 1; n >= 0; n--) {
				String line = lines.get(n);

				if (!line.trim().isEmpty() && detectIndentation(line).length() < lineIndentation.length()) {
					index = n + 1;
					break;
				}
			}
			String newLine = lineIndentation + startExpression + GENERATED_MARKER;

			if (index == lineNumber) {
				newLine += "\n" + lineIndentation + indentation + "pass" + GENERATED_MARKER;
			}

			lines.add(index, newLine);
		}

		return lines;
	}

	/*
	 * Checks if last line is one of the start/end patterns and makes it a valid
	 * statement.
	 */
	private List<String> fixLastLine(List<String> lines, String indentation) {
		lines = new ArrayList<>(lines);

		String lastLine = lines.get(lines.size() - 1);

		if (lineMatches(lastLine, TRY_END_PATTERNS)) {
			lines.add(detectIndentation(lastLine) + indentation + "raise" + GENERATED_MARKER);
		} else if (lineMatches(lastLine, IF_START_PATTERNS) || lineMatches(lastLine, IF_END_PATTERNS)
						|| lineMatches(lastLine, TRY_START_PATTERNS)) {
			lines.add(detectIndentation(lastLine) + indentation + "pass" + GENERATED_MARKER);
		}

		return lines;
	}

	private List<String> fixFirstLine(List<String> lines) {
		lines = new ArrayList<>(lines);

		if (!detectIndentation(lines.get(0)).isEmpty()) {
			lines.add(0, "if True:" + GENERATED_MARKER);
		}

		return lines;
	}

	/*
	 * Checks if the given line matches at least one of the given patterns.
	 */
	private boolean lineMatches(String line, Pattern[] patterns) {
		return Arrays.asList(patterns).stream().anyMatch((pattern) -> pattern.matcher(line).matches());
	}

	private static final Pattern INDENTATION_PATTERN = Pattern.compile("(\\s+).+?");

	/*
	 * Detects the shortest sequence of whitespaces with length > 0). If there is no
	 * such sequence, it returns an empty String.
	 */
	private String detectIndentation(String script) {
		String indentation = "";

		for (String line : getLines(script)) {
			Matcher m = INDENTATION_PATTERN.matcher(line);
			if (m.matches()) {
				if (indentation.isEmpty() || m.group(1).length() < indentation.length()) {
					indentation = m.group(1);
				}
			}
		}
		return indentation;
	}

	private List<String> getLines(String script) {
		return Arrays.asList(script.split("\n"));
	}
	//</editor-fold>


	@Override
  public ICodeGenerator getCodeGenerator() {
    return new JythonCodeGenerator();
  }
}
