Prism.languages.ebnf = {
	'comment': /\(\*[\s\S]+?\*\)/,
	'important': /\?[^\?\r\n]*\?/,
	'keyword': /\b([A-Za-z][A-Za-z0-9_]*)\b/,
	'string': {
		pattern: /("|')(\\(?:\r\n|[\s\S])|(?!\1)[^\\\r\n])*\1/,
		greedy: true
	},
	'operator': /=/,
	'punctuation': /,;\|\[\]\{\}\(\)-/
};
