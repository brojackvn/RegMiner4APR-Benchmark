
package org.gitlab4j.api.webhook;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)
public class PushEvent extends AbstractPushEvent implements Event {

    public static final String X_GITLAB_EVENT = "Push Hook";
    public static final String OBJECT_KIND = "push";

    @Override
    public String getObjectKind() {
        return (OBJECT_KIND);
    }

    public void setObjectKind(String objectKind) {
        if (!OBJECT_KIND.equals(objectKind)) {
            throw new RuntimeException("Invalid object_kind (" + objectKind + "), must be '" + OBJECT_KIND + "'");
        }
    }
}
