/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of Wabit.
 *
 * Wabit is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Wabit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package ca.sqlpower.wabit;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A collection of static utility methods for dealing with variables.
 * 
 * <h2>Variables</h2>
 * 
 * Variable references take the form ${name}, where name is the name of the
 * variable being referenced. The variable name must be alphanumeric (a-z, 0-9,
 * underscore) and is case sensitive. Currently, only simple substitution is
 * possible; in the future, we plan to provide a rich set of modifiers similar
 * to those described in the Parameter Expansion section of the zshexpn(1) man
 * page.
 */
public class Variables {

    /**
     * Substitutes any number of variable references in the given string, returning
     * the resultant string with all variable references replaced by the corresponding
     * variable values.
     * 
     * @param textWithVars
     * @param variableContext
     * @return
     */
    public static String substitute(String textWithVars, VariableContext variableContext) {
        Pattern p = Pattern.compile("\\$\\{([$a-zA-Z0-9_]+)\\}");
        
        StringBuilder text = new StringBuilder();
        Matcher matcher = p.matcher(textWithVars);
        
        int currentIndex = 0;
        while (!matcher.hitEnd()) {
            if (matcher.find()) {
                String variableName = matcher.group(1);
                String variableValue;
                if (variableName.equals("$")) {
                    variableValue = "$";
                } else {
                    variableValue = variableContext.getVariableValue(variableName, "MISSING_VAR:"+variableName);
                }
                text.append(textWithVars.substring(currentIndex, matcher.start()));
                text.append(variableValue);
                currentIndex = matcher.end();
            }  
        }
        
        text.append(textWithVars.substring(currentIndex));
        
        return text.toString();
    }
}
