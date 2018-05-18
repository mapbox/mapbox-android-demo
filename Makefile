checkstyle:
	./gradlew checkstyle

graph:
	./gradlew generateDependencyGraphMapboxLibraries

sanity-test-example-activities:
	node scripts/generate-test-code.js