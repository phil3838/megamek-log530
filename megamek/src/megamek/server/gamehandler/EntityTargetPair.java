package megamek.server.gamehandler;

import megamek.common.Entity;
import megamek.common.Targetable;
import megamek.common.annotations.Nullable;

import java.util.Objects;

public class EntityTargetPair {
        Entity ent;

        Targetable target;

        EntityTargetPair (Entity e, Targetable t) {
            ent = e;
            target = t;
        }

        @Override
        public boolean equals(@Nullable Object o) {
            if (this == o) {
                return true;
            } else if ((null == o) || (getClass() != o.getClass())) {
                return false;
            } else {
                final megamek.server.gamehandler.EntityTargetPair other = (megamek.server.gamehandler.EntityTargetPair) o;
                return Objects.equals(ent, other.ent) && Objects.equals(target, other.target);
            }
        }

        @Override
        public int hashCode() {
            return Objects.hash(ent, target);
        }
}
