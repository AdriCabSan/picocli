/*
   Copyright 2017 Remko Popma

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package picocli;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import org.junit.Test;

import picocli.CommandLine.CommandSpec;
import picocli.CommandLine.Help.Ansi;
import picocli.CommandLine.OptionSpec;
import picocli.CommandLine.PositionalParamSpec;
import picocli.CommandLine.Range;

import static org.junit.Assert.*;

public class CommandLineModelTest {
    private static String usageString(Object annotatedObject, Ansi ansi) throws
            UnsupportedEncodingException {
        return usageString(new CommandLine(annotatedObject), ansi);
    }
    private static String usageString(CommandLine commandLine, Ansi ansi) throws UnsupportedEncodingException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        commandLine.usage(new PrintStream(baos, true, "UTF8"), ansi);
        String result = baos.toString("UTF8");

        if (ansi == Ansi.AUTO) {
            baos.reset();
            commandLine.usage(new PrintStream(baos, true, "UTF8"));
            assertEquals(result, baos.toString("UTF8"));
        } else if (ansi == Ansi.ON) {
            baos.reset();
            commandLine.usage(new PrintStream(baos, true, "UTF8"), CommandLine.Help.defaultColorScheme(Ansi.ON));
            assertEquals(result, baos.toString("UTF8"));
        }
        return result;
    }

    @Test
    public void testEmptyModelHelp() throws Exception {
        CommandSpec spec = new CommandSpec();
        CommandLine commandLine = new CommandLine(spec);
        String actual = usageString(commandLine, Ansi.OFF);
        assertEquals(String.format("Usage: <main class>%n"), actual);
    }

    @Test
    public void testEmptyModelParse() throws Exception {
        System.setProperty("picocli.trace", "OFF");
        CommandSpec spec = new CommandSpec();
        CommandLine commandLine = new CommandLine(spec);
        commandLine.setUnmatchedArgumentsAllowed(true);
        commandLine.parse("-p", "123", "abc");
        assertEquals(Arrays.asList("-p", "123", "abc"), commandLine.getUnmatchedArguments());
    }

    @Test
    public void testModelHelp() throws Exception {
        CommandSpec spec = new CommandSpec();
        spec.add(new OptionSpec().names("-h", "--help").usageHelp(true).description("show help and exit"));
        spec.add(new OptionSpec().names("-V", "--version").usageHelp(true).description("show help and exit"));
        spec.add(new OptionSpec().names("-c", "--count").paramLabel("COUNT").arity("1").type(int.class).description("number of times to execute"));
        CommandLine commandLine = new CommandLine(spec);
        String actual = usageString(commandLine, Ansi.OFF);
        String expected = String.format("" +
                "Usage: <main class> [-hV] [-c=COUNT]%n" +
                "  -c, --count=COUNT           number of times to execute%n" +
                "  -h, --help                  show help and exit%n" +
                "  -V, --version               show help and exit%n");
        assertEquals(expected, actual);
    }

    @Test
    public void testModelParse() throws Exception {
        CommandSpec spec = new CommandSpec();
        spec.add(new OptionSpec().names("-h", "--help").usageHelp(true).description("show help and exit"));
        spec.add(new OptionSpec().names("-V", "--version").usageHelp(true).description("show help and exit"));
        spec.add(new OptionSpec().names("-c", "--count").paramLabel("COUNT").arity("1").type(int.class).description("number of times to execute"));
        CommandLine commandLine = new CommandLine(spec);
        commandLine.parse("-c", "33");
        assertEquals(33, spec.optionsMap().get("-c").getValue());
    } // TODO parse method should return an object offering only the options/positionals that were matched

    // TODO tests that verify CommandSpec.validate()

    @Test
    public void testOptionDefaultTypeIsBoolean() throws Exception {
        assertEquals(boolean.class, new OptionSpec("-x").validate().type());
    }

    @Test
    public void testOptionDefaultArityIsZero() throws Exception {
        assertEquals(Range.valueOf("0"), new OptionSpec("-x").validate().arity());
    }

    @Test
    public void testOptionDefaultSplitRegexIsEmptyString() throws Exception {
        assertEquals("", new OptionSpec("-x").validate().splitRegex());
    }
    @Test
    public void testPositionalDefaultSplitRegexIsEmptyString() throws Exception {
        assertEquals("", new PositionalParamSpec().validate().splitRegex());
    }

    @Test
    public void testOptionDefaultDescriptionIsEmptyArray() throws Exception {
        assertArrayEquals(new String[0], new OptionSpec("-x").validate().description());
    }
    @Test
    public void testPositionalDefaultDescriptionIsEmptyArray() throws Exception {
        assertArrayEquals(new String[0], new PositionalParamSpec().validate().description());
    }

    @Test
    public void testOptionDefaultParamLabel() throws Exception {
        assertEquals("PARAM", new OptionSpec("-x").validate().paramLabel());
    }
    @Test
    public void testPositionalDefaultParamLabel() throws Exception {
        assertEquals("PARAM", new PositionalParamSpec().validate().paramLabel());
    }

    @Test
    public void testOptionDefaultAuxiliaryTypesIsType() throws Exception {
        assertArrayEquals(new Class[] {boolean.class}, new OptionSpec("-x").validate().auxiliaryTypes());
        assertArrayEquals(new Class[] {int.class}, new OptionSpec("-x").type(int.class).validate().auxiliaryTypes());
    }
    @Test
    public void testPositionalDefaultAuxiliaryTypesIsType() throws Exception {
        assertArrayEquals(new Class[] {String.class}, new PositionalParamSpec().validate().auxiliaryTypes());
        assertArrayEquals(new Class[] {int.class}, new PositionalParamSpec().type(int.class).validate().auxiliaryTypes());
    }

    @Test
    public void testOptionWithArityHasDefaultTypeBoolean() throws Exception {
        assertEquals(boolean.class, new OptionSpec("-x").arity("0").validate().type());
        assertEquals(boolean.class, new OptionSpec("-x").arity("1").validate().type());
        assertEquals(boolean.class, new OptionSpec("-x").arity("0..1").validate().type());
        assertEquals(boolean[].class, new OptionSpec("-x").arity("2").validate().type());
        assertEquals(boolean[].class, new OptionSpec("-x").arity("0..2").validate().type());
        assertEquals(boolean[].class, new OptionSpec("-x").arity("*").validate().type());
    }

    @Test
    public void testOptionAuxiliaryTypeOverridesDefaultType() throws Exception {
        assertEquals(int.class, new OptionSpec("-x").auxiliaryTypes(int.class).validate().type());
        assertEquals(int.class, new OptionSpec("-x").arity("0").auxiliaryTypes(int.class).validate().type());
        assertEquals(int.class, new OptionSpec("-x").arity("1").auxiliaryTypes(int.class).validate().type());
        assertEquals(int.class, new OptionSpec("-x").arity("0..1").auxiliaryTypes(int.class).validate().type());
        assertEquals(int.class, new OptionSpec("-x").arity("2").auxiliaryTypes(int.class).validate().type());
        assertEquals(int.class, new OptionSpec("-x").arity("0..2").auxiliaryTypes(int.class).validate().type());
        assertEquals(int.class, new OptionSpec("-x").arity("*").auxiliaryTypes(int.class).validate().type());
    }

    @Test
    public void testPositionalDefaultTypeIsString() throws Exception {
        assertEquals(String.class, new PositionalParamSpec().validate().type());
    }

    @Test
    public void testPositionalDefaultIndexIsAll() throws Exception {
        assertEquals(Range.valueOf("*"), new PositionalParamSpec().validate().index());
    }

    @Test
    public void testPositionalDefaultArityIsOne() throws Exception {
        assertEquals(Range.valueOf("1"), new PositionalParamSpec().validate().arity());
    }

    @Test
    public void testPositionalWithArityHasDefaultTypeString() throws Exception {
        assertEquals(String.class, new PositionalParamSpec().arity("0").validate().type());
        assertEquals(String.class, new PositionalParamSpec().arity("1").validate().type());
        assertEquals(String.class, new PositionalParamSpec().arity("0..1").validate().type());
        assertEquals(String[].class, new PositionalParamSpec().arity("2").validate().type());
        assertEquals(String[].class, new PositionalParamSpec().arity("0..2").validate().type());
        assertEquals(String[].class, new PositionalParamSpec().arity("*").validate().type());
    }

    @Test
    public void testPositionalAuxiliaryTypeOverridesDefaultType() throws Exception {
        assertEquals(int.class, new PositionalParamSpec().auxiliaryTypes(int.class).validate().type());
        assertEquals(int.class, new PositionalParamSpec().arity("0").auxiliaryTypes(int.class).validate().type());
        assertEquals(int.class, new PositionalParamSpec().arity("1").auxiliaryTypes(int.class).validate().type());
        assertEquals(int.class, new PositionalParamSpec().arity("0..1").auxiliaryTypes(int.class).validate().type());
        assertEquals(int.class, new PositionalParamSpec().arity("2").auxiliaryTypes(int.class).validate().type());
        assertEquals(int.class, new PositionalParamSpec().arity("0..2").auxiliaryTypes(int.class).validate().type());
        assertEquals(int.class, new PositionalParamSpec().arity("*").auxiliaryTypes(int.class).validate().type());
    }
}
