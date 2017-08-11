Prism.languages.ddl = {
	'comment': [
		/\/\*[\s\S]+?\*\//,
		/\/\/.*/
	],
	'keyword':  [
		/\b(?:DOMAIN|PROBLEM|TEMPORAL_MODULE|PAR_TYPE|COMP_TYPE|VALUE|MEETS|COMPONENT|SYNCHRONIZE|REQUIREMENT|PRODUCTION|CONSUMPTION)\b/,
		/\b(?:SingletonStateVariable|SimpleGroundStateVariable|NumericParameterType|EnumerationParameterType)\b/,
		/\b(?:INF)\b/,
		/\b(?:FLEXIBLE|BOUNDED|ESTA_LIGHT|ESTA_LIGHT_MAX_CONSUMPTION)/,
		/\b(?:MEETS|MET-BY|BEFORE|AFTER|EQUALS|STARTS|STARTED-BY|FINISHES|FINISHED-BY|DURING|CONTAINS|OVERLAPS|OVERLAPPED-BY|STARTS-AT|ENDS-AT|AT-START|AT-END|BEFORE-START|AFTER-END|START-START|START-END|END-START|END-END|CONTAINS-START|CONTAINS-END|STARTS-DURING|ENDS-DURING)\b/
	],
	'variable': /\?\w+/,
	'number': /([+-]|\b)\d+/,
	'operator': /=|<=?|>=?|!=|[+\-*]|/,
	'punctuation': /\(\.|\.\)|[()\[\]:;,.]/
};
