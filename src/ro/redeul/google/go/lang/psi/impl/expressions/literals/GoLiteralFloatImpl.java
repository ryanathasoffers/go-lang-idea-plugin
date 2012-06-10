package ro.redeul.google.go.lang.psi.impl.expressions.literals;

import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;
import ro.redeul.google.go.lang.psi.expressions.literals.GoLiteralFloat;
import ro.redeul.google.go.lang.psi.impl.GoPsiElementBase;

/**
 * Author: Toader Mihai Claudiu <mtoader@gmail.com>
 * <p/>
 * Date: 6/2/11
 * Time: 3:44 AM
 */
public class GoLiteralFloatImpl extends GoPsiElementBase
    implements GoLiteralFloat {

    public GoLiteralFloatImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public Float getValue() {
        return null;
    }

    @Override
    public Type getType() {
        return Type.Float;
    }
}
