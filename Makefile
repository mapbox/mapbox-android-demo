checkstyle:
	./gradlew checkstyle && ./gradlew lintKotlin

graph:
	./gradlew generateDependencyGraphMapboxLibraries

sanity-test-example-activities:
	node scripts/generate-test-code.js

kotlin-lint:
	./gradlew lintKotlin
