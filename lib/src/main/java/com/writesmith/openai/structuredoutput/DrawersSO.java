package com.writesmith.openai.structuredoutput;

import com.oaigptconnector.model.JSONSchema;
import com.oaigptconnector.model.JSONSchemaParameter;

import java.util.List;

@JSONSchema(name = "Output_In_Drawers", functionDescription = "A drawer is a UI item that has a title, and when the title is tapped the content shows.", strict = JSONSchema.NullableBool.TRUE)
public class DrawersSO {

    public static class Drawer {

        @JSONSchemaParameter(name = "Index")
        private Integer index;

        @JSONSchemaParameter(name = "Title")
        private String title;

        @JSONSchemaParameter(name = "Content")
        private String content;

        public Drawer() {

        }

        public Drawer(Integer index, String title, String content) {
            this.index = index;
            this.title = title;
            this.content = content;
        }

        public Integer getIndex() {
            return index;
        }

        public String getTitle() {
            return title;
        }

        public String getContent() {
            return content;
        }

    }

    @JSONSchemaParameter(name = "Title", description = "A fitting title for this collection of drawers.")
    private String title;

    @JSONSchemaParameter(name = "Drawers")
    private List<Drawer> drawers;

    public DrawersSO() {

    }

    public DrawersSO(String title, List<Drawer> drawers) {
        this.title = title;
        this.drawers = drawers;
    }

    public String getTitle() {
        return title;
    }

    public List<Drawer> getDrawers() {
        return drawers;
    }

}
