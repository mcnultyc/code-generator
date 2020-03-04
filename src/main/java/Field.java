/**
 * @author Carlos Antonio McNulty
 */


import com.github.javaparser.ast.Modifier;


/**
 *
 */
class Field {
    public final Modifier.Keyword access;
    public final String type;
    public final String name;

    public Field(String type, String name, Modifier.Keyword access){
        this.type = type;
        this.name = name;
        this.access = access;
    }
}
