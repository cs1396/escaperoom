package cs1396.escaperoom.game.entities.conditions;

import java.util.logging.Logger;

public enum ConditionalType {

  EmptyConditional(EmptyConditional.class),
  PlayerHasItem(PlayerHasItem.class),
  ItemsAreToggled(ItemsAreToggled.class),
  ItemContainsItem(ItemContainsItem.class),
  AlwaysActive(AlwaysActive.class),
  ItemInArea(ItemInArea.class),
  ItemIsActive(ItemIsActive.class),
  ItemsArePowered(ItemsArePowered.class),
  PlayerInRegion(PlayerInTiles.class),
  AndConditional(AndConditional.class),
  OrConditional(OrConditional.class),
  XORConditional(XORConditional.class),
  NotConditional(NotConditional.class),
  WasNeverTrue(WasNeverTrue.class),
  AllOf(AllOf.class),
  SequenceConditional(SequenceConditional.class);

  private Class<? extends Conditional> clz;
  private Logger log = Logger.getLogger(ConditionalType.class.getName());

  private ConditionalType(Class <?extends Conditional> clz){
    this.clz = clz;
  }

  public Conditional getConditional(){
    try {
      return this.clz.getDeclaredConstructor().newInstance();
    } catch (Exception e){
      log.warning("Conditiional type " + clz.getName() + " does not define a default constructor" );
      return null;
    }
  }

}
