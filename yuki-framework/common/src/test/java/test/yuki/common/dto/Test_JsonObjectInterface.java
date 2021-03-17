package test.yuki.common.dto;


import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import io.vertx.core.json.JsonObject;
import yuki.common.dto.Get;
import yuki.common.dto.JsonDto;
import yuki.common.dto.Set;

interface TestDto extends JsonDto {
    @Set("string")
    TestDto setString(String val);

    @Set("integer")
    TestDto setInterger(Integer val);

    @Set("double")
    TestDto setDouble(Double val);

    @Get("test")
    String setTest();

}

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class Test_JsonObjectInterface {

    @Test
    void Should_CreateSimpleObject__When_JsonObjectProjectPassed() {
        JsonObject obj = new JsonObject();

        obj.put("new","test");

        TestDto dto = JsonDto.cast(obj,TestDto.class);

        dto.setDouble(1.0);

        dto.setInterger(10);
        dto.setString("name");

        Assertions.assertThat(obj.getMap())
                .isNotNull()
                .containsKeys("double", "integer", "string")
                .containsValues(1.0,10,"name");
    }

    @Test
    void Should_ReturnNullInstance__When_JsonObjectNull() {
        TestDto dto = JsonDto.cast(null,TestDto.class);

        Assertions.assertThat(dto)
                .isNull();
    }

    @Test
    void Should_ReturnValues__When_JsonObjectHasAttributes() {
        JsonObject object=new JsonObject().put("test","test123");

        TestDto dto = JsonDto.cast(object,TestDto.class);

        Assertions.assertThat(dto)
                .isInstanceOf(TestDto.class)
                .isNotNull();

        Assertions.assertThat(dto.setTest())
                .isEqualTo("test123");
    }

    @Test
    void Should_Values__When_ChaninSetIsUsed() {
        JsonObject obj = new JsonObject();

        obj.put("new","test");

        TestDto dto = JsonDto.cast(obj,TestDto.class);

        dto.setDouble(1.0).setInterger(10).setString("name");

        Assertions.assertThat(obj.getMap())
                .isNotNull()
                .containsKeys("double", "integer", "string")
                .containsValues(1.0,10,"name");
    }

    @Test
    void Should_ReturnInternalInstance__When_Asked() {
        JsonObject obj = new JsonObject();

        obj.put("new","test");

        TestDto dto = JsonDto.cast(obj,TestDto.class);

        dto.setDouble(1.0).setInterger(10).setString("name");

        Assertions.assertThat(dto.getJsonObject().toString())
                .isNotNull()
                .isEqualTo(obj.toString());
    }
}