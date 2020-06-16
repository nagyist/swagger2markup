/*
 * Copyright 2017 Robert Winkler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.swagger2markup;

import io.github.swagger2markup.assertions.DiffUtils;
import io.github.swagger2markup.builder.Swagger2MarkupConfigBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.assertj.core.api.BDDAssertions.assertThat;

public class AsciidocConverterTest {

    private static final String[] EXPECTED_FILES = new String[]{"definitions.adoc", "overview.adoc", "paths.adoc", "security.adoc"};
    private List<String> expectedFiles;

    @Before
    public void setUp() {
        expectedFiles = new ArrayList<>(asList(EXPECTED_FILES));
    }

    @Test
    public void testToString() throws URISyntaxException {
        //Given
        Path file = Paths.get(AsciidocConverterTest.class.getResource("/yaml/swagger_petstore.yaml").toURI());

        //When
        String asciiDocAsString = Swagger2MarkupConverter.from(file).build()
                .toString();
        //Then
        assertThat(asciiDocAsString).isNotEmpty();
    }

    @Test
    public void testToFolder() throws URISyntaxException {
        //Given
        Path file = Paths.get(AsciidocConverterTest.class.getResource("/yaml/swagger_petstore.yaml").toURI());
        Path outputDirectory = Paths.get("build/test/asciidoc/to_folder");
        FileUtils.deleteQuietly(outputDirectory.toFile());

        //When
        Swagger2MarkupConverter.from(file).build()
                .toFolder(outputDirectory);

        //Then
        String[] files = outputDirectory.toFile().list();
        assertThat(files).hasSize(4).containsAll(expectedFiles);

        Path expectedFilesDirectory = Paths.get(AsciidocConverterTest.class.getResource("/expected/asciidoc/to_folder").toURI());
        DiffUtils.assertThatAllFilesAreEqual(expectedFilesDirectory, outputDirectory, "testToFolder.html");
    }

    @Test
    public void testToFileWithoutExtension() throws URISyntaxException {
        //Given
        Path file = Paths.get(AsciidocConverterTest.class.getResource("/yaml/swagger_petstore.yaml").toURI());
        Path outputFile = Paths.get("build/test/asciidoc/to_file/swagger.adoc");

        //When
        Swagger2MarkupConverter.from(file)
                .build()
                .toFileWithoutExtension(outputFile);

        //Then
        Path expectedFile = Paths.get(AsciidocConverterTest.class.getResource("/expected/asciidoc/to_file/swagger.adoc").toURI());
        DiffUtils.assertThatFileIsEqual(expectedFile, outputFile, "testToFileWithoutExtension.html");
    }

    @Test(expected = NullPointerException.class)
    // Not working atm. See https://github.com/Swagger2Markup/swagger2markup/issues/212
    public void testModularizedSwaggerSpec() throws URISyntaxException {
        //Given
        Path file = Paths.get(AsciidocConverterTest.class.getResource("/yaml/modules/swagger_petstore.yaml").toURI());
        Path outputFile = Paths.get("build/test/asciidoc/modularized_swagger/swagger.adoc");

        //When
        Swagger2MarkupConverter.from(file)
                .build()
                .toFileWithoutExtension(outputFile);

        //Then
        Path expectedFile = Paths.get(AsciidocConverterTest.class.getResource("/expected/asciidoc/to_file/swagger.adoc").toURI());
        DiffUtils.assertThatFileIsEqual(expectedFile, outputFile, "testModularizedSwaggerSpec.html");
    }

    @Test
    public void testOrderByAsIs() throws URISyntaxException {
        //Given
        Path file = Paths.get(AsciidocConverterTest.class.getResource("/yaml/swagger_ordering.yaml").toURI());
        Path outputDirectory = Paths.get("build/test/asciidoc/ordering_asis");
        FileUtils.deleteQuietly(outputDirectory.toFile());

        //When
        Swagger2MarkupConfig config =  new Swagger2MarkupConfigBuilder()
                .withOutputLanguage(Language.BR)
                .withTagOrdering(OrderBy.AS_IS)
                .withParameterOrdering(OrderBy.AS_IS)
                .withOperationOrdering(OrderBy.AS_IS)
                .withDefinitionOrdering(OrderBy.AS_IS)
                .withPathsGroupedBy(GroupBy.TAGS)
                .build();

        Swagger2MarkupConverter.from(file).withConfig(config).build()
                .toFolder(outputDirectory);

        //Then
        String[] files = outputDirectory.toFile().list();
        assertThat(files).hasSize(4).containsAll(expectedFiles);

        Path expectedFilesDirectory = Paths.get(AsciidocConverterTest.class.getResource("/expected/asciidoc/ordering_asis").toURI());
        DiffUtils.assertThatAllFilesAreEqual(expectedFilesDirectory, outputDirectory, "testOrderingAsIs.html");
    }

    @Test
    public void testOrderByNatural() throws URISyntaxException {
        //Given
        Path file = Paths.get(AsciidocConverterTest.class.getResource("/yaml/swagger_ordering.yaml").toURI());
        Path outputDirectory = Paths.get("build/test/asciidoc/ordering_natural");
        FileUtils.deleteQuietly(outputDirectory.toFile());

        Swagger2MarkupConfig config =  new Swagger2MarkupConfigBuilder()
                .withTagOrdering(OrderBy.NATURAL)
                .withParameterOrdering(OrderBy.NATURAL)
                .withOperationOrdering(OrderBy.NATURAL)
                .withPathsGroupedBy(GroupBy.TAGS)
                .build();

        //When
        Swagger2MarkupConverter.from(file).withConfig(config).build()
                .toFolder(outputDirectory);

        //Then
        String[] files = outputDirectory.toFile().list();
        assertThat(files).hasSize(4).containsAll(expectedFiles);

        Path expectedFilesDirectory = Paths.get(AsciidocConverterTest.class.getResource("/expected/asciidoc/ordering_natural").toURI());
        DiffUtils.assertThatAllFilesAreEqual(expectedFilesDirectory, outputDirectory, "testOrderingNatural.html");
    }

    @Test
    public void testOrderByRegex() throws URISyntaxException {
        //Given
        Path file = Paths.get(AsciidocConverterTest.class.getResource("/yaml/swagger_ordering_regex.yaml").toURI());
        Path outputDirectory = Paths.get("build/test/asciidoc/ordering_regex");
        FileUtils.deleteQuietly(outputDirectory.toFile());

        Swagger2MarkupConfig config =  new Swagger2MarkupConfigBuilder()
                .withTagOrdering(OrderBy.NATURAL)
                .withParameterOrdering(OrderBy.NATURAL)
                .withOperationOrdering(OrderBy.NATURAL)
                .withPathsGroupedBy(GroupBy.REGEX)
                .withHeaderRegex("\\/(\\w+)(\\/|\\w)*$")
                .build();

        //When
        Swagger2MarkupConverter.from(file).withConfig(config).build()
                .toFolder(outputDirectory);

        //Then
        String[] files = outputDirectory.toFile().list();
        assertThat(files).hasSize(4).containsAll(expectedFiles);

        Path expectedFilesDirectory = Paths.get(AsciidocConverterTest.class.getResource("/expected/asciidoc/ordering_regex").toURI());
        DiffUtils.assertThatAllFilesAreEqual(expectedFilesDirectory, outputDirectory, "testOrderingRegex.html");
    }

    @Test
    public void testMarkupRenderingInInstagram() throws URISyntaxException {
        //Given
        Path file = Paths.get(AsciidocConverterTest.class.getResource("/yaml/swagger_instagram.yaml").toURI());
        Path outputDirectory = Paths.get("build/test/asciidoc/instagram");
        FileUtils.deleteQuietly(outputDirectory.toFile());

        //When
        Swagger2MarkupConfig config =  new Swagger2MarkupConfigBuilder()
                .withTagOrdering(OrderBy.AS_IS)
                .build();

        Swagger2MarkupConverter.from(file).withConfig(config).build()
                .toFolder(outputDirectory);

        //Then
        String[] files = outputDirectory.toFile().list();
        assertThat(files).hasSize(4).containsAll(expectedFiles);

        Path expectedFilesDirectory = Paths.get(AsciidocConverterTest.class.getResource("/expected/asciidoc/instagram").toURI());
        DiffUtils.assertThatAllFilesAreEqual(expectedFilesDirectory, outputDirectory, "testMarkupRenderingInInstagram.html");
    }

    @Test
    public void testInterDocumentCrossReferences() throws URISyntaxException {
        //Given
        Path file = Paths.get(AsciidocConverterTest.class.getResource("/yaml/swagger_petstore.yaml").toURI());
        Path outputDirectory = Paths.get("build/test/asciidoc/idxref");
        FileUtils.deleteQuietly(outputDirectory.toFile());

        //When
        Swagger2MarkupConfig config =  new Swagger2MarkupConfigBuilder()
                .withInterDocumentCrossReferences()
                .build();

        Swagger2MarkupConverter.from(file).withConfig(config).build()
                .toFolder(outputDirectory);

        //Then
        String[] files = outputDirectory.toFile().list();
        assertThat(files).hasSize(4).containsAll(expectedFiles);

        Path expectedFilesDirectory = Paths.get(AsciidocConverterTest.class.getResource("/expected/asciidoc/idxref").toURI());
        DiffUtils.assertThatAllFilesAreEqual(expectedFilesDirectory, outputDirectory, "testInterDocumentCrossReferences.html");
    }

    @Test
    public void testWithBasePathPrefix() throws URISyntaxException {
        //Given
        Path file = Paths.get(AsciidocConverterTest.class.getResource("/json/swagger_examples.json").toURI());
        Path outputDirectory = Paths.get("build/test/asciidoc/basepathprefix");
        FileUtils.deleteQuietly(outputDirectory.toFile());

        //When
        Swagger2MarkupConfig config =  new Swagger2MarkupConfigBuilder()
                .withBasePathPrefix()
                .withGeneratedExamples()
                .build();

        Swagger2MarkupConverter.from(file).withConfig(config).build()
                .toFolder(outputDirectory);

        //Then
        String[] files = outputDirectory.toFile().list();
        assertThat(files).hasSize(4).containsAll(expectedFiles);

        Path expectedFilesDirectory = Paths.get(AsciidocConverterTest.class.getResource("/expected/asciidoc/basepathprefix").toURI());
        DiffUtils.assertThatAllFilesAreEqual(expectedFilesDirectory, outputDirectory, "testWithBasePathPrefix.html");
    }

    @Test
    public void testSwagger2AsciiDocConversionFromString() throws IOException, URISyntaxException {
        //Given
        String swaggerJsonString = IOUtils.toString(getClass().getResourceAsStream("/yaml/swagger_petstore.yaml"), StandardCharsets.UTF_8);
        Path outputDirectory = Paths.get("build/test/asciidoc/to_folder");
        FileUtils.deleteQuietly(outputDirectory.toFile());

        //When
        Swagger2MarkupConverter.from(swaggerJsonString).build()
                .toFolder(outputDirectory);

        //Then
        String[] files = outputDirectory.toFile().list();
        assertThat(files).hasSize(4).containsAll(expectedFiles);

        Path expectedFilesDirectory = Paths.get(AsciidocConverterTest.class.getResource("/expected/asciidoc/to_folder").toURI());
        DiffUtils.assertThatAllFilesAreEqual(expectedFilesDirectory, outputDirectory, "testSwagger2AsciiDocConversion.html");
    }

    @Test
    public void testWithExamples() throws IOException, URISyntaxException {
        //Given
        String swaggerJsonString = IOUtils.toString(getClass().getResourceAsStream("/json/swagger_examples.json"), StandardCharsets.UTF_8);
        Path outputDirectory = Paths.get("build/test/asciidoc/examples");
        FileUtils.deleteQuietly(outputDirectory.toFile());

        //When
        Swagger2MarkupConfig config = new Swagger2MarkupConfigBuilder()
                .build();

        Swagger2MarkupConverter.from(swaggerJsonString)
                .withConfig(config)
                .build()
                .toFolder(outputDirectory);

        //Then
        String[] files = outputDirectory.toFile().list();
        assertThat(files).hasSize(4).containsAll(expectedFiles);
        Path expectedFilesDirectory = Paths.get(AsciidocConverterTest.class.getResource("/expected/asciidoc/examples").toURI());
        DiffUtils.assertThatAllFilesAreEqual(expectedFilesDirectory, outputDirectory, "testWithExamples.html");
    }

    @Test
    public void testWithGeneratedExamples() throws IOException, URISyntaxException {
        //Given
        String swaggerJsonString = IOUtils.toString(getClass().getResourceAsStream("/json/swagger_examples.json"), StandardCharsets.UTF_8);
        Path outputDirectory = Paths.get("build/test/asciidoc/generated_examples");
        FileUtils.deleteQuietly(outputDirectory.toFile());

        //When
        Swagger2MarkupConfig config =  new Swagger2MarkupConfigBuilder()
                .withGeneratedExamples()
                .build();

        Swagger2MarkupConverter.from(swaggerJsonString)
                .withConfig(config)
                .build()
                .toFolder(outputDirectory);

        //Then
        String[] files = outputDirectory.toFile().list();
        assertThat(files).hasSize(4).containsAll(expectedFiles);
        Path expectedFilesDirectory = Paths.get(AsciidocConverterTest.class.getResource("/expected/asciidoc/generated_examples").toURI());
        DiffUtils.assertThatAllFilesAreEqual(expectedFilesDirectory, outputDirectory, "testWithGeneratedExamples.html");
    }

    @Test
    public void testWithGeneratedRecursiveExamples() throws IOException, URISyntaxException {
        // Given
        String swaggerJsonString = IOUtils.toString(getClass().getResourceAsStream("/json/swagger_recursion.json"), StandardCharsets.UTF_8);
        Path outputDirectory = Paths.get("build/test/asciidoc/generated_recursion_examples");
        FileUtils.deleteQuietly(outputDirectory.toFile());

        // When
        Swagger2MarkupConfig config =  new Swagger2MarkupConfigBuilder().withoutInlineSchema().withGeneratedExamples().build();

        Swagger2MarkupConverter.from(swaggerJsonString).withConfig(config).build().toFolder(outputDirectory);

        // Then
        String[] files = outputDirectory.toFile().list();
        assertThat(files).hasSize(4).containsAll(expectedFiles);
        Path expectedFilesDirectory = Paths.get(AsciidocConverterTest.class.getResource("/expected/asciidoc/generated_recursion_examples").toURI());
        DiffUtils.assertThatAllFilesAreEqual(expectedFilesDirectory, outputDirectory, "testWithGeneratedRecursiveExamples.html");
    }


    @Test
    public void testWithGeneratedExamplesAndRequiredQueryParameters() throws IOException, URISyntaxException {
        //Given
        String swaggerJsonString = IOUtils.toString(getClass().getResourceAsStream("/yaml/swagger_examples_required_parameters.yaml"), StandardCharsets.UTF_8);
        Path outputDirectory = Paths.get("build/test/asciidoc/generated_examples_required_parameters");
        FileUtils.deleteQuietly(outputDirectory.toFile());

        //When
        Swagger2MarkupConfig config =  new Swagger2MarkupConfigBuilder()
            .withGeneratedExamples()
            .build();

        Swagger2MarkupConverter.from(swaggerJsonString)
            .withConfig(config)
            .build()
            .toFolder(outputDirectory);

        //Then
        String[] files = outputDirectory.toFile().list();
        assertThat(files).hasSize(4).containsAll(expectedFiles);
        Path expectedFilesDirectory = Paths.get(AsciidocConverterTest.class.getResource("/expected/asciidoc/generated_examples_required_parameters").toURI());
        DiffUtils.assertThatAllFilesAreEqual(expectedFilesDirectory, outputDirectory, "testWithGeneratedExamplesAndRequiredParameters.html");
    }

    @Test
    public void testWithGeneratedInlineResponseExamples() throws IOException, URISyntaxException {
        //Given
        String swaggerJsonString = IOUtils.toString(getClass().getResourceAsStream("/yaml/swagger_examples_inline_response.yaml"), StandardCharsets.UTF_8);
        Path outputDirectory = Paths.get("build/test/asciidoc/generated_examples_inline_response");
        FileUtils.deleteQuietly(outputDirectory.toFile());

        //When
        Swagger2MarkupConfig config =  new Swagger2MarkupConfigBuilder()
            .withGeneratedExamples()
            .build();

        Swagger2MarkupConverter.from(swaggerJsonString)
            .withConfig(config)
            .build()
            .toFolder(outputDirectory);

        //Then
        String[] files = outputDirectory.toFile().list();
        assertThat(files).hasSize(4).containsAll(expectedFiles);
        Path expectedFilesDirectory = Paths.get(AsciidocConverterTest.class.getResource("/expected/asciidoc/generated_examples_inline_response").toURI());
        DiffUtils.assertThatAllFilesAreEqual(expectedFilesDirectory, outputDirectory, "testWithGeneratedInlineResponseExamples.html");
    }

    @Test
    public void testWithExamplesAndMimeTypesSuffix() throws IOException, URISyntaxException {
        //Given
        String swaggerJsonString = IOUtils.toString(getClass().getResourceAsStream("/json/swagger_examples_suffix.json"), StandardCharsets.UTF_8);
        Path outputDirectory = Paths.get("build/test/asciidoc/examples_suffix");
        FileUtils.deleteQuietly(outputDirectory.toFile());

        //When
        Swagger2MarkupConfig config = new Swagger2MarkupConfigBuilder()
                .build();

        Swagger2MarkupConverter.from(swaggerJsonString)
                .withConfig(config)
                .build()
                .toFolder(outputDirectory);

        //Then
        String[] files = outputDirectory.toFile().list();
        assertThat(files).hasSize(4).containsAll(expectedFiles);
        Path expectedFilesDirectory = Paths.get(AsciidocConverterTest.class.getResource("/expected/asciidoc/examples_suffix").toURI());
        DiffUtils.assertThatAllFilesAreEqual(expectedFilesDirectory, outputDirectory, "testWithExamples.html");
    }

    @Test
    public void testWithInlineSchema() throws URISyntaxException {
        //Given
        Path file = Paths.get(AsciidocConverterTest.class.getResource("/yaml/swagger_inlineSchema.yaml").toURI());
        Path outputDirectory = Paths.get("build/test/asciidoc/inline_schema");
        FileUtils.deleteQuietly(outputDirectory.toFile());

        //When
        Swagger2MarkupConfig config = new Swagger2MarkupConfigBuilder()
                .build();
        Swagger2MarkupConverter.from(file)
                .withConfig(config)
                .build()
                .toFolder(outputDirectory);

        //Then
        String[] files = outputDirectory.toFile().list();
        assertThat(files).hasSize(4).containsAll(expectedFiles);
        Path expectedFilesDirectory = Paths.get(AsciidocConverterTest.class.getResource("/expected/asciidoc/inline_schema").toURI());
        DiffUtils.assertThatAllFilesAreEqual(expectedFilesDirectory, outputDirectory, "testWithInlineSchema.html");
    }

    @Test
    public void testWithInlineSchemaAndFlatBody() throws URISyntaxException {
        //Given
        Path file = Paths.get(AsciidocConverterTest.class.getResource("/yaml/swagger_inlineSchema.yaml").toURI());
        Path outputDirectory = Paths.get("build/test/asciidoc/inline_schema_flat_body");
        FileUtils.deleteQuietly(outputDirectory.toFile());

        //When
        Swagger2MarkupConfig config =  new Swagger2MarkupConfigBuilder()
                .withFlatBody()
                .build();
        Swagger2MarkupConverter.from(file)
                .withConfig(config)
                .build()
                .toFolder(outputDirectory);

        //Then
        String[] files = outputDirectory.toFile().list();
        assertThat(files).hasSize(4).containsAll(expectedFiles);
        Path expectedFilesDirectory = Paths.get(AsciidocConverterTest.class.getResource("/expected/asciidoc/inline_schema_flat_body").toURI());
        DiffUtils.assertThatAllFilesAreEqual(expectedFilesDirectory, outputDirectory, "testWithInlineSchemaAndFlatBody.html");
    }

    @Test
    public void testGroupedByTags() throws URISyntaxException {
        //Given
        Path file = Paths.get(AsciidocConverterTest.class.getResource("/yaml/swagger_petstore.yaml").toURI());
        Path outputDirectory = Paths.get("build/test/asciidoc/group_by_tags");
        FileUtils.deleteQuietly(outputDirectory.toFile());
        //When
        Swagger2MarkupConfig config =  new Swagger2MarkupConfigBuilder()
                .withPathsGroupedBy(GroupBy.TAGS)
                .build();
        Swagger2MarkupConverter.from(file)
                .withConfig(config)
                .build()
                .toFolder(outputDirectory);

        //Then
        String[] files = outputDirectory.toFile().list();
        assertThat(files).hasSize(4).containsAll(expectedFiles);
        Path expectedFilesDirectory = Paths.get(AsciidocConverterTest.class.getResource("/expected/asciidoc/group_by_tags").toURI());
        DiffUtils.assertThatAllFilesAreEqual(expectedFilesDirectory, outputDirectory, "testGroupedByTags.html");
    }

    @Test
    public void testByTagsWithMissingTag() throws URISyntaxException {
        //Given
        Path file = Paths.get(AsciidocConverterTest.class.getResource("/json/swagger_missing_tag.json").toURI());
        Path outputDirectory = Paths.get("build/test/asciidoc/generated");
        FileUtils.deleteQuietly(outputDirectory.toFile());
        //When
        try {
            Swagger2MarkupConfig config = new Swagger2MarkupConfigBuilder()
                    .withPathsGroupedBy(GroupBy.TAGS)
                    .build();

            Swagger2MarkupConverter.from(file)
                    .withConfig(config)
                    .build()
                    .toFolder(outputDirectory);
            // If NullPointerException was not thrown, test would fail the specified message
            failBecauseExceptionWasNotThrown(NullPointerException.class);
        } catch (Exception e) {
            assertThat(e).hasMessage("Can't GroupBy.TAGS. Operation 'updatePet' has no tags");
        }
    }

    @Test
    public void tesDoesNotContainUriScheme() throws IOException, URISyntaxException {
        //Given
        Path file = Paths.get(AsciidocConverterTest.class.getResource("/yaml/swagger_should_not_contain_uri_scheme.yaml").toURI());
        Path outputDirectory = Paths.get("build/test/asciidoc/generated");
        FileUtils.deleteQuietly(outputDirectory.toFile());

        //When
        Swagger2MarkupConverter.from(file).build()
                .toFolder(outputDirectory);

        //Then
        String[] files = outputDirectory.toFile().list();
        assertThat(files).hasSize(4).containsAll(expectedFiles);

        assertThat(new String(Files.readAllBytes(outputDirectory.resolve("overview.adoc"))))
                .doesNotContain("=== URI scheme");
    }

    @Test
    public void testContainsUriScheme() throws IOException, URISyntaxException {
        //Given
        Path file = Paths.get(AsciidocConverterTest.class.getResource("/yaml/swagger_should_contain_uri_scheme.yaml").toURI());
        Path outputDirectory = Paths.get("build/test/asciidoc/generated");
        FileUtils.deleteQuietly(outputDirectory.toFile());

        //When
        Swagger2MarkupConverter.from(file).build()
                .toFolder(outputDirectory);

        //Then
        String[] files = outputDirectory.toFile().list();
        assertThat(files).hasSize(4).containsAll(expectedFiles);

        assertThat(new String(Files.readAllBytes(outputDirectory.resolve("overview.adoc"))))
                .contains("=== URI scheme");
    }


    @Test
    public void testWithSeparatedDefinitions() throws IOException, URISyntaxException {
        //Given
        Path file = Paths.get(AsciidocConverterTest.class.getResource("/yaml/swagger_petstore.yaml").toURI());
        Path outputDirectory = Paths.get("build/test/asciidoc/generated");
        FileUtils.deleteQuietly(outputDirectory.toFile());

        //When
        Swagger2MarkupConfig config = new Swagger2MarkupConfigBuilder()
                .withSeparatedDefinitions()
                .build();
        Swagger2MarkupConverter.from(file).withConfig(config).build()
                .toFolder(outputDirectory);

        //Then
        String[] files = outputDirectory.toFile().list();
        expectedFiles.add("definitions");
        assertThat(files).hasSize(5).containsAll(expectedFiles);

        Path definitionsDirectory = outputDirectory.resolve("definitions");
        String[] definitions = definitionsDirectory.toFile().list();
        assertThat(definitions).hasSize(5).containsAll(
                asList("Category.adoc", "Order.adoc", "Pet.adoc", "Tag.adoc", "User.adoc"));
    }

    @Test
    public void testWithSeparatedOperations() throws URISyntaxException {
        //Given
        Path file = Paths.get(AsciidocConverterTest.class.getResource("/yaml/swagger_petstore.yaml").toURI());
        Path outputDirectory = Paths.get("build/test/asciidoc/generated");
        FileUtils.deleteQuietly(outputDirectory.toFile());

        //When
        Swagger2MarkupConfig config =  new Swagger2MarkupConfigBuilder()
                .withSeparatedOperations()
                .build();
        Swagger2MarkupConverter.from(file).withConfig(config).build()
                .toFolder(outputDirectory);

        //Then
        String[] files = outputDirectory.toFile().list();
        expectedFiles.add("operations");
        assertThat(files).hasSize(5).containsAll(expectedFiles);

        Path pathsDirectory = outputDirectory.resolve("operations");
        String[] paths = pathsDirectory.toFile().list();
        assertThat(paths).hasSize(18);
    }

    @Test
    public void testWithAsciidocContentInTables() throws URISyntaxException {

        //Given
        Path file = Paths.get(AsciidocConverterTest.class.getResource("/yaml/swagger_petstore_with_adoc_content.yaml").toURI());
        Path outputDirectory = Paths.get("build/test/asciidoc/generated");
        FileUtils.deleteQuietly(outputDirectory.toFile());

        //When
        Swagger2MarkupConfig config =  new Swagger2MarkupConfigBuilder()
                .withSwaggerMarkupLanguage(MarkupLanguage.ASCIIDOC)
                .build();

        Swagger2MarkupConverter.from(file).withConfig(config).build()
                .toFolder(outputDirectory);

        //Then
        String[] files = outputDirectory.toFile().list();
        assertThat(files).hasSize(4).containsAll(expectedFiles);

    }

    @Test
    public void testWithRussianOutputLanguage() throws IOException, URISyntaxException {
        testWithOutputLanguage(Language.RU, "definitions.adoc", "== Определения");
    }

    @Test
    public void testWithFrenchOutputLanguage() throws IOException, URISyntaxException {
        testWithOutputLanguage(Language.FR, "overview.adoc", "== Sch\u00E9ma d'URI");
    }

    @Test
    public void testWithGermanOutputLanguage() throws IOException, URISyntaxException {
        testWithOutputLanguage(Language.DE, "definitions.adoc", "Beschreibung");
    }

    @Test
    public void testWithSpanishOutputLanguage() throws IOException, URISyntaxException {
        testWithOutputLanguage(Language.ES, "definitions.adoc", "Descripción");
    }

    @Test
    public void testWithJapaneseOutputLanguage() throws IOException, URISyntaxException {
        testWithOutputLanguage(Language.JA, "definitions.adoc", "説明");
    }

    @Test
    public void testWithChineseOutputLanguage() throws IOException, URISyntaxException {
        testWithOutputLanguage(Language.ZH, "definitions.adoc", "说明");
    }

    private void testWithOutputLanguage(Language language, String outputFilename, String expected) throws IOException, URISyntaxException {
        //Given
        Path file = Paths.get(AsciidocConverterTest.class.getResource("/yaml/swagger_petstore.yaml").toURI());
        Path outputDirectory = Paths.get("build/test/asciidoc/language");
        FileUtils.deleteQuietly(outputDirectory.toFile());

        //When
        Swagger2MarkupConfig config =  new Swagger2MarkupConfigBuilder()
                .withOutputLanguage(language)
                .build();
        Swagger2MarkupConverter.from(file)
                .withConfig(config)
                .build()
                .toFolder(outputDirectory);

        //Then
        assertThat(new String(Files.readAllBytes(outputDirectory.resolve(outputFilename)), StandardCharsets.UTF_8))
                .contains(expected);
    }

    @Test
    public void testWithMaps() throws URISyntaxException {
        //Given
        Path file = Paths.get(AsciidocConverterTest.class.getResource("/json/swagger_maps.json").toURI());
        Path outputDirectory = Paths.get("build/test/asciidoc/maps");
        FileUtils.deleteQuietly(outputDirectory.toFile());

        //When
        Swagger2MarkupConfig config =  new Swagger2MarkupConfigBuilder()
                .withTagOrdering(OrderBy.AS_IS)
                .build();
        Swagger2MarkupConverter.from(file)
                .withConfig(config)
                .build()
                .toFolder(outputDirectory);

        //Then
        String[] files = outputDirectory.toFile().list();
        assertThat(files).hasSize(4).containsAll(expectedFiles);

        Path expectedFilesDirectory = Paths.get(AsciidocConverterTest.class.getResource("/expected/asciidoc/maps").toURI());
        DiffUtils.assertThatAllFilesAreEqual(expectedFilesDirectory, outputDirectory, "testWithMaps.html");
    }

    @Test
    public void testWithEnums() throws URISyntaxException {
        //Given
        Path file = Paths.get(AsciidocConverterTest.class.getResource("/json/swagger_enums.json").toURI());
        Path outputDirectory = Paths.get("build/test/asciidoc/enums");
        FileUtils.deleteQuietly(outputDirectory.toFile());

        Map<String, String> configMap = new HashMap<>();
        configMap.put("swagger2markup.generatedExamplesEnabled", "true");  // enable examples generation
        //When
        Swagger2MarkupConfig config =  new Swagger2MarkupConfigBuilder(configMap)
                .withMarkupLanguage(MarkupLanguage.ASCIIDOC)
                .build();
        Swagger2MarkupConverter.from(file)
                .withConfig(config)
                .build()
                .toFolder(outputDirectory);

        //Then
        String[] files = outputDirectory.toFile().list();
        assertThat(files).hasSize(4).containsAll(expectedFiles);

        Path expectedFilesDirectory = Paths.get(AsciidocConverterTest.class.getResource("/expected/asciidoc/enums").toURI());
        DiffUtils.assertThatAllFilesAreEqual(expectedFilesDirectory, outputDirectory, "testWithEnums.html");
    }


    @Test
    public void testWithValidators() throws URISyntaxException {
        //Given
        Path file = Paths.get(AsciidocConverterTest.class.getResource("/json/swagger_validators.json").toURI());
        Path outputDirectory = Paths.get("build/test/asciidoc/validators");
        FileUtils.deleteQuietly(outputDirectory.toFile());

        //When
        Swagger2MarkupConfig config = new Swagger2MarkupConfigBuilder()
                .build();
        Swagger2MarkupConverter.from(file)
                .withConfig(config)
                .build()
                .toFolder(outputDirectory);

        //Then
        String[] files = outputDirectory.toFile().list();
        assertThat(files).hasSize(4).containsAll(expectedFiles);

        Path expectedFilesDirectory = Paths.get(AsciidocConverterTest.class.getResource("/expected/asciidoc/validators").toURI());
        DiffUtils.assertThatAllFilesAreEqual(expectedFilesDirectory, outputDirectory, "testWithValidators.html");
    }

    @Test
    public void testWithPolymorphism() throws URISyntaxException {
        //Given
        Path file = Paths.get(AsciidocConverterTest.class.getResource("/json/swagger_polymorphism.json").toURI());
        Path outputDirectory = Paths.get("build/test/asciidoc/polymorphism");
        FileUtils.deleteQuietly(outputDirectory.toFile());

        //When
        Swagger2MarkupConfig config = new Swagger2MarkupConfigBuilder()
                .build();
        Swagger2MarkupConverter.from(file)
                .withConfig(config)
                .build()
                .toFolder(outputDirectory);

        //Then
        String[] files = outputDirectory.toFile().list();
        assertThat(files).hasSize(4).containsAll(expectedFiles);

        Path expectedFilesDirectory = Paths.get(AsciidocConverterTest.class.getResource("/expected/asciidoc/polymorphism").toURI());
        DiffUtils.assertThatAllFilesAreEqual(expectedFilesDirectory, outputDirectory, "testWithPolymorphism.html");
    }

    @Test
    public void testWithPolymorphismAsIsOrdering() throws URISyntaxException {
        //Given
        Path file = Paths.get(AsciidocConverterTest.class.getResource("/json/swagger_polymorphism.json").toURI());
        Path outputDirectory = Paths.get("build/test/asciidoc/polymorphismAsIsOrdering");
        FileUtils.deleteQuietly(outputDirectory.toFile());

        //When
        Swagger2MarkupConfig config =  new Swagger2MarkupConfigBuilder()
                .withPropertyOrdering(OrderBy.AS_IS)
                .build();
        Swagger2MarkupConverter.from(file)
                .withConfig(config)
                .build()
                .toFolder(outputDirectory);

        //Then
        String[] files = outputDirectory.toFile().list();
        assertThat(files).hasSize(4).containsAll(expectedFiles);

        Path expectedFilesDirectory = Paths.get(AsciidocConverterTest.class.getResource("/expected/asciidoc/polymorphismAsIsOrdering").toURI());
        DiffUtils.assertThatAllFilesAreEqual(expectedFilesDirectory, outputDirectory, "testWithPolymorphismAsIsOrdering.html");
    }

    @Test
    public void testWithResponseHeaders() throws URISyntaxException {
        //Given
        Path file = Paths.get(AsciidocConverterTest.class.getResource("/yaml/swagger_response_headers.yaml").toURI());
        Path outputDirectory = Paths.get("build/test/asciidoc/response_headers");
        FileUtils.deleteQuietly(outputDirectory.toFile());

        //When
        Swagger2MarkupConfig config = new Swagger2MarkupConfigBuilder()
                .build();
        Swagger2MarkupConverter.from(file)
                .withConfig(config)
                .build()
                .toFolder(outputDirectory);

        //Then
        String[] files = outputDirectory.toFile().list();
        assertThat(files).hasSize(4).containsAll(expectedFiles);

        Path expectedFilesDirectory = Paths.get(AsciidocConverterTest.class.getResource("/expected/asciidoc/response_headers").toURI());
        DiffUtils.assertThatAllFilesAreEqual(expectedFilesDirectory, outputDirectory, "testWithResponseHeaders.html");
    }

    @Test
    public void testWithEmptyContactUsingJSON() throws URISyntaxException {
        //Given
        Path file = Paths.get(AsciidocConverterTest.class.getResource("/json/swagger_emptycontact.json").toURI());
        Path outputDirectory = Paths.get("build/test/asciidoc/emptycontact");
        FileUtils.deleteQuietly(outputDirectory.toFile());

        //When
        Swagger2MarkupConfig config = new Swagger2MarkupConfigBuilder()
                .build();
        Swagger2MarkupConverter.from(file)
                .withConfig(config)
                .build()
                .toFolder(outputDirectory);

        //Then
        String[] files = outputDirectory.toFile().list();
        assertThat(files).hasSize(4).containsAll(expectedFiles);

        Path expectedFilesDirectory = Paths.get(AsciidocConverterTest.class.getResource("/expected/asciidoc/emptycontact").toURI());
        DiffUtils.assertThatAllFilesAreEqual(expectedFilesDirectory, outputDirectory, "testWithEmptyContactUsingJSON.html");
    }

    @Test
    public void testWithFormat() throws URISyntaxException {
        //Given
        Path file = Paths.get(AsciidocConverterTest.class.getResource("/yaml/swagger_format.yaml").toURI());
        Path outputDirectory = Paths.get("build/test/asciidoc/format");
        FileUtils.deleteQuietly(outputDirectory.toFile());

        //When
        Swagger2MarkupConfig config = new Swagger2MarkupConfigBuilder()
                .build();
        Swagger2MarkupConverter.from(file)
                .withConfig(config)
                .build()
                .toFolder(outputDirectory);

        //Then
        String[] files = outputDirectory.toFile().list();
        assertThat(files).hasSize(4).containsAll(expectedFiles);

        Path expectedFilesDirectory = Paths.get(AsciidocConverterTest.class.getResource("/expected/asciidoc/format").toURI());
        DiffUtils.assertThatAllFilesAreEqual(expectedFilesDirectory, outputDirectory, "testWithFormat.html");
    }

    @Test
    public void testWithPageBreaks() throws IOException, URISyntaxException {
        //Given
        String swaggerJsonString = IOUtils.toString(getClass().getResourceAsStream("/json/swagger_examples.json"), StandardCharsets.UTF_8);
        Path outputDirectory = Paths.get("build/test/asciidoc/page_breaks");
        FileUtils.deleteQuietly(outputDirectory.toFile());

        //When
        Swagger2MarkupConfig config =  new Swagger2MarkupConfigBuilder()
                .withPageBreaks(new ArrayList<>(asList(SwaggerPageBreakLocations.BEFORE_OPERATION, SwaggerPageBreakLocations.BEFORE_OPERATION_EXAMPLE_REQUEST)))
                .build();

        Swagger2MarkupConverter.from(swaggerJsonString)
                .withConfig(config)
                .build()
                .toFolder(outputDirectory);

        //Then
        String[] files = outputDirectory.toFile().list();
        assertThat(files).hasSize(4).containsAll(expectedFiles);
        Path expectedFilesDirectory = Paths.get(AsciidocConverterTest.class.getResource("/expected/asciidoc/page_breaks").toURI());
        DiffUtils.assertThatAllFilesAreEqual(expectedFilesDirectory, outputDirectory, "testWithPageBreaks.html");
    }

    @Test
    public void testWithEmptyTables() throws IOException, URISyntaxException {
        //Given
        String swaggerJsonString = IOUtils.toString(getClass().getResourceAsStream("/json/swagger_empty_tables.json"), StandardCharsets.UTF_8);
        Path outputDirectory = Paths.get("build/test/asciidoc/empty_tables");
        FileUtils.deleteQuietly(outputDirectory.toFile());

        //When
        Swagger2MarkupConfig config =  new Swagger2MarkupConfigBuilder()
                .withPageBreaks(new ArrayList<>(asList(SwaggerPageBreakLocations.BEFORE_OPERATION, SwaggerPageBreakLocations.BEFORE_OPERATION_EXAMPLE_REQUEST)))
                .build();

        Swagger2MarkupConverter.from(swaggerJsonString)
                .withConfig(config)
                .build()
                .toFolder(outputDirectory);

        //Then
        String[] files = outputDirectory.toFile().list();
        assertThat(files).hasSize(4).containsAll(expectedFiles);
        Path expectedFilesDirectory = Paths.get(AsciidocConverterTest.class.getResource("/expected/asciidoc/empty_tables").toURI());
        DiffUtils.assertThatAllFilesAreEqual(expectedFilesDirectory, outputDirectory, "testWithEmptyTables.html");
    }

    @Test
    public void testExamplesInUrl() throws IOException, URISyntaxException {
        //Given
        String swaggerJsonString = IOUtils.toString(getClass().getResourceAsStream("/yaml/swagger_url_examples.yaml"), StandardCharsets.UTF_8);
        Path outputDirectory = Paths.get("build/test/asciidoc/url_examples");
        FileUtils.deleteQuietly(outputDirectory.toFile());

        //When
        Swagger2MarkupConfig config =  new Swagger2MarkupConfigBuilder()
                .withPageBreaks(new ArrayList<>(asList(SwaggerPageBreakLocations.BEFORE_OPERATION, SwaggerPageBreakLocations.BEFORE_OPERATION_EXAMPLE_REQUEST)))
                .withGeneratedExamples()
                .build();

        Swagger2MarkupConverter.from(swaggerJsonString)
                .withConfig(config)
                .build()
                .toFolder(outputDirectory);

        //Then
        String[] files = outputDirectory.toFile().list();
        assertThat(files).hasSize(4).containsAll(expectedFiles);
        Path expectedFilesDirectory = Paths.get(AsciidocConverterTest.class.getResource("/expected/asciidoc/url_examples").toURI());
        DiffUtils.assertThatAllFilesAreEqual(expectedFilesDirectory, outputDirectory, "testExamplesInUrl.html");
    }

    @Test
    public void testRequestExamplesWithCurl() throws IOException, URISyntaxException  {
        //Given
        String swaggerJsonString = IOUtils.toString(getClass().getResourceAsStream("/yaml/swagger_url_examples.yaml"), StandardCharsets.UTF_8);
        Path outputDirectory = Paths.get("build/test/asciidoc/url_examples_using_curl");
        FileUtils.deleteQuietly(outputDirectory.toFile());

        //When
        Swagger2MarkupConfig config =  new Swagger2MarkupConfigBuilder()
                .withPageBreaks(new ArrayList<>(asList(SwaggerPageBreakLocations.BEFORE_OPERATION, SwaggerPageBreakLocations.BEFORE_OPERATION_EXAMPLE_REQUEST)))
                .withRequestExamplesFormat("curl")
                .withGeneratedExamples()
                .build();

        Swagger2MarkupConverter.from(swaggerJsonString)
                .withConfig(config)
                .build()
                .toFolder(outputDirectory);

        //Then
        String[] files = outputDirectory.toFile().list();
        assertThat(files).hasSize(4).containsAll(expectedFiles);
        Path expectedFilesDirectory = Paths.get(AsciidocConverterTest.class.getResource("/expected/asciidoc/url_examples_using_curl").toURI());
        DiffUtils.assertThatAllFilesAreEqual(expectedFilesDirectory, outputDirectory, "testExamplesInUrlUsingCurl.html");
    }

    @Test
    public void testRequestExamplesWithInvokeWebRequest() throws IOException, URISyntaxException {
        //Given
        String swaggerJsonString = IOUtils.toString(getClass().getResourceAsStream("/yaml/swagger_url_examples.yaml"), StandardCharsets.UTF_8);
        Path outputDirectory = Paths.get("build/test/asciidoc/url_examples_using_invoke_webrequest");
        FileUtils.deleteQuietly(outputDirectory.toFile());

        //When
        Swagger2MarkupConfig config =  new Swagger2MarkupConfigBuilder()
                .withPageBreaks(new ArrayList<>(asList(SwaggerPageBreakLocations.BEFORE_OPERATION, SwaggerPageBreakLocations.BEFORE_OPERATION_EXAMPLE_REQUEST)))
                .withRequestExamplesFormat("invoke-webrequest")
                .withGeneratedExamples()
                .build();

        Swagger2MarkupConverter.from(swaggerJsonString)
                .withConfig(config)
                .build()
                .toFolder(outputDirectory);

        //Then
        String[] files = outputDirectory.toFile().list();
        assertThat(files).hasSize(4).containsAll(expectedFiles);
        Path expectedFilesDirectory = Paths.get(AsciidocConverterTest.class.getResource("/expected/asciidoc/url_examples_using_invoke_webrequest").toURI());
        DiffUtils.assertThatAllFilesAreEqual(expectedFilesDirectory, outputDirectory, "testRequestExamplesWithInvokeWebRequest.html");
    }

    @Test
    public void testRequestExamplesWithAllSettings() throws IOException, URISyntaxException {
        //Given
        String swaggerJsonString = IOUtils.toString(getClass().getResourceAsStream("/yaml/swagger_petstore_body_examples.yaml"), StandardCharsets.UTF_8);
        Path outputDirectory = Paths.get("build/test/asciidoc/url_examples_using_all_settings");
        FileUtils.deleteQuietly(outputDirectory.toFile());

        //When
        Swagger2MarkupConfig config =  new Swagger2MarkupConfigBuilder()
                .withPageBreaks(new ArrayList<>(asList(SwaggerPageBreakLocations.BEFORE_OPERATION, SwaggerPageBreakLocations.BEFORE_OPERATION_EXAMPLE_REQUEST)))
                .withGeneratedExamples()
                .withRequestExamplesFormat("curl")
                .withRequestExamplesHideBasePath(false)
                .withRequestExamplesIncludeAllQueryParams(true)
                .withRequestExamplesSourceFormat("console")
                .withRequestExamplesHost("example.com")
                .withRequestExamplesSchema("HTTPS")
                .withRequestExamplesQueryArrayStyle("multiple[]")
                .build();

        Swagger2MarkupConverter.from(swaggerJsonString)
                .withConfig(config)
                .build()
                .toFolder(outputDirectory);

        //Then
        String[] files = outputDirectory.toFile().list();
        assertThat(files).hasSize(4).containsAll(expectedFiles);
        Path expectedFilesDirectory = Paths.get(AsciidocConverterTest.class.getResource("/expected/asciidoc/url_examples_using_all_settings").toURI());
        DiffUtils.assertThatAllFilesAreEqual(expectedFilesDirectory, outputDirectory, "testRequestExamplesWithAllSettings.html");
    }
}
