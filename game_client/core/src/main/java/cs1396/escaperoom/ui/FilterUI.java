package cs1396.escaperoom.ui;

import java.util.Optional;
import java.util.function.BiFunction;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Payload;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Source;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Target;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Null;

import cs1396.escaperoom.screens.AbstractScreen;
import cs1396.escaperoom.services.Types.Filter;
import cs1396.escaperoom.services.Types.FilterConstType;
import cs1396.escaperoom.services.Types.Filters;
import cs1396.escaperoom.services.Types.IsField;
import cs1396.escaperoom.services.Types.Sort;
import cs1396.escaperoom.services.Types.Filters.Combination;
import cs1396.escaperoom.services.Types.Sort.Direction;
import cs1396.escaperoom.ui.notifications.Notifier;
import cs1396.escaperoom.ui.widgets.G24Dialog;
import cs1396.escaperoom.ui.widgets.G24TextButton;
import cs1396.escaperoom.ui.widgets.G24TextInput;
import cs1396.escaperoom.ui.widgets.G24Label;

public class FilterUI extends G24Dialog {
  DragAndDrop dragAndDrop = new DragAndDrop();

  public <E extends Enum<E> & IsField> FilterUI(BiFunction<Filters, Sort, Void> onSearch, Class<E> fieldType) {
    super("Sort and Filter Results");
    fieldSources.space(10);
    fieldSources.columnLeft();
    methodSources.space(10);
    methodSources.columnLeft();

    dragAndDrop.addTarget(new ReturnBin(fieldSources));
    dragAndDrop.addTarget(new ReturnBin(methodSources));

    IsField aFieldInstance = null;
    for (IsField f : fieldType.getEnumConstants()) {
      fieldSources.addActor(new FieldSourceLabel(f));
      aFieldInstance = f;
    }

    for (Filter.Method m : Filter.Method.values()) {
      methodSources.addActor(new MethodSourceLabel(m));
    }

    box = new SearchBuilderBox(aFieldInstance.defaultSort());

    G24TextButton searchButton = new G24TextButton("Search");
    searchButton.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        if (searchButton.isChecked()) {
          searchButton.setChecked(false);
          FilterUI.this.setVisible(false);
          onSearch.apply(getFilters(), getSort());
        }
      }
    });

    G24TextButton closeButton = new G24TextButton("Close");
    closeButton.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        if (closeButton.isChecked()) {
          closeButton.setChecked(false);
          FilterUI.this.setVisible(false);
        }
      }
    });


    getContentTable().add(new G24Label("Fields", "title")).center();
    getContentTable().add(new G24Label("Methods", "title")).center().row();
    getContentTable().add(fieldSources).top();
    getContentTable().add(methodSources).top();
    getContentTable().add(box).top().row();
    getButtonTable().add(searchButton);
    getButtonTable().add(closeButton);
  }

  private static String filterDisplayString(Filter.Method m) {
    switch (m) {
      case Equals:
        return "Equals";
      case GreaterThan:
        return "Greater than";
      case GreaterThanEqualTo:
        return "Greater than equal to";
      case LessThan:
        return "Less than";
      case LessThanEqualTo:
        return "Less than or equal to";
      case NotEquals:
        return "Not equal to";
      default:
        break;
    }
    return "<invalid filter type>";
  }

  private class FieldBankSource extends DragAndDrop.Source {
    IsField field;

    public FieldBankSource(Actor sourceActor, IsField field) {
      super(sourceActor);
      this.field = field;
    }

    @Override
    public Payload dragStart(InputEvent event, float x, float y, int pointer) {
      return new FieldPayload(this.field);
    }
  }

  private class MethodBankSource extends DragAndDrop.Source {
    Filter.Method method;

    public MethodBankSource(Actor sourceActor, Filter.Method method) {
      super(sourceActor);
      this.method = method;
    }

    @Override
    public Payload dragStart(InputEvent event, float x, float y, int pointer) {
      return new MethodPayload(this.method);
    }
  }

  private class FilterEntry extends HorizontalGroup {
    Optional<Filter.Method> method = Optional.empty();
    Optional<IsField> field = Optional.empty();
    private G24Label fieldLabel, methodLabel;
    private MethodTarget methodTarget;
    private FieldTarget fieldTarget;
    private MethodSource methodSource;
    private FieldSource fieldSource;

    private G24TextInput valueInput;

    public boolean isComplete() {
      return method.isPresent() && field.isPresent();
    }

    public FilterEntry() {
      space(5);

      valueInput = new G24TextInput();

      methodLabel = new G24Label("<empty>", "bubble");
      methodTarget = new MethodTarget(methodLabel);

      fieldLabel = new G24Label("<empty>", "bubble");
      fieldTarget = new FieldTarget(fieldLabel);

      dragAndDrop.addTarget(methodTarget);
      dragAndDrop.addTarget(fieldTarget);

      methodSource = new MethodSource();
      fieldSource = new FieldSource();

      addActor(fieldLabel);
      addActor(methodLabel);
      addActor(valueInput);

      ImageButton removeFilterButton = new ImageButton(AbstractScreen.skin, "toggleForbidden");
      removeFilterButton.addListener(new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
          FilterEntry.this.remove();
          FilterUI.this.pack();
        }
      });
      addActor(removeFilterButton);
    }

    public void clearMethod() {
      method = Optional.empty();
      methodLabel.setText("<empty>");
      methodLabel.pack();
      FilterUI.this.pack();

      dragAndDrop.removeSource(methodSource);
      dragAndDrop.addTarget(methodTarget);
    }

    public void setMethod(Filter.Method filter) {
      method = Optional.of(filter);
      methodLabel.setText(filterDisplayString(filter));
      methodLabel.pack();
      FilterUI.this.pack();

      dragAndDrop.removeTarget(methodTarget);
      dragAndDrop.addSource(methodSource);
    }

    public void clearField() {
      this.field = Optional.empty();
      fieldLabel.setText("<empty>");
      fieldLabel.pack();

      FilterUI.this.pack();

      valueInput.setFilter(c -> true);

      dragAndDrop.removeSource(fieldSource);
      dragAndDrop.addTarget(fieldTarget);
    }

    public void setField(IsField field) {
      this.field = Optional.of(field);
      dragAndDrop.removeTarget(fieldTarget);
      dragAndDrop.addSource(fieldSource);
      fieldLabel.setText(field.displayName());
      fieldLabel.pack();
      FilterUI.this.pack();
      switch (field.getConstType()) {
        case Integer:
          valueInput.setFilter(c -> Character.isDigit(c));
          break;
        case String:
          valueInput.setFilter(c -> true);
          break;
        default:
          break;
      }
    }

    private class MethodTarget extends DragAndDrop.Target {

      public MethodTarget(Actor target) {
        super(target);
      }

      @Override
      public boolean drag(Source source, Payload payload, float x, float y, int pointer) {
        if (!(source instanceof FilterUI.MethodBankSource)) {
          return false;
        }
        if (FilterEntry.this.method.isPresent()) {
          return false;
        }
        return true;
      }

      @Override
      public void drop(Source source, Payload payload, float x, float y, int pointer) {
        MethodPayload s = MethodPayload.class.cast(payload);
        FilterEntry.this.setMethod(s.getMethod());
      }

    }

    private class FieldTarget extends DragAndDrop.Target {
      public FieldTarget(Actor target) {
        super(target);
      }

      @Override
      public boolean drag(Source source, Payload payload, float x, float y, int pointer) {
        if (!(source instanceof FilterUI.FieldBankSource)) {
          return false;
        }
        if (FilterEntry.this.field.isPresent()) {
          return false;
        }
        return true;
      }

      @Override
      public void drop(Source source, Payload payload, float x, float y, int pointer) {
        FieldPayload s = FieldPayload.class.cast(payload);
        FilterEntry.this.setField(s.getField());
      }

    }

    private class FieldSource extends DragAndDrop.Source {
      FieldSource() {
        super(fieldLabel);
      }

      @Override
      public void dragStop(InputEvent event, float x, float y, int pointer, @Null Payload payload,
          @Null Target target) {
        fieldLabel.setColor(1, 1, 1, 1);
        if (target != null) {
          FilterEntry.this.clearField();
        }
      }

      @Override
      public Payload dragStart(InputEvent event, float x, float y, int pointer) {
        fieldLabel.setColor(1, 1, 1, 0.2f);
        return new FieldPayload(FilterEntry.this.field.get());
      }

    }

    private class MethodSource extends DragAndDrop.Source {
      MethodSource() {
        super(methodLabel);
      }

      @Override
      public void dragStop(InputEvent event, float x, float y, int pointer, @Null Payload payload,
          @Null Target target) {
        methodLabel.setColor(1, 1, 1, 1);
        if (target != null) {
          FilterEntry.this.clearMethod();
        }
      }

      @Override
      public Payload dragStart(InputEvent event, float x, float y, int pointer) {
        methodLabel.setColor(1, 1, 1, 0.2f);
        return new MethodPayload(FilterEntry.this.method.get());
      }

    }
  }

  private class SearchBuilderBox extends VerticalGroup {

    private G24TextButton addFilterButton;
    private IsField sortField = null;
    private Combination combination = Combination.And;
    private Direction sortDir = Direction.Descending;

    public SearchBuilderBox(IsField defaultSortField) {
      columnLeft();
      space(10);
      this.sortField = defaultSortField;
      addFilterButton = new G24TextButton("Add Filter");
      addFilterButton.setProgrammaticChangeEvents(false);
      addFilterButton.addListener(new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
          if (addFilterButton.isChecked()) {
            addFilterButton.setChecked(false);
            addEmptyFilterEntry();
            FilterUI.this.pack();
          }
        }
      });

      HorizontalGroup filterComboBox = new HorizontalGroup();
      ButtonGroup<CheckBox> filterComboBtnGroup = new ButtonGroup<>();

      CheckBox andCombo = new CheckBox("And", AbstractScreen.skin);
      CheckBox orCombo = new CheckBox("Or", AbstractScreen.skin);
      filterComboBtnGroup.add(andCombo, orCombo);
      andCombo.setChecked(true);

      filterComboBox.addActor(new G24Label("Filter Combination:", "title"));
      filterComboBox.addActor(andCombo);
      filterComboBox.addActor(orCombo);

      andCombo.addListener(new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
          if (andCombo.isChecked()) {
            combination = Combination.And;
          }
        }
      });
      orCombo.addListener(new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
          if (orCombo.isChecked()) {
            combination = Combination.Or;
          }
        }
      });

      HorizontalGroup sortOnBox = new HorizontalGroup();
      G24Label fieldLabel = new G24Label(defaultSortField.displayName(), "bubble");
      ButtonGroup<CheckBox> sortDirGroup = new ButtonGroup<>();

      CheckBox ascCheck = new CheckBox("Ascending", AbstractScreen.skin);
      CheckBox descCheck = new CheckBox("Descending", AbstractScreen.skin);
      sortDirGroup.add(ascCheck, descCheck);
      descCheck.setChecked(true);

      ascCheck.addListener(new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
          if (ascCheck.isChecked()) {
            sortDir = Direction.Ascending;
          }
        }
      });
      descCheck.addListener(new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
          if (descCheck.isChecked()) {
            sortDir = Direction.Descending;
          }
        }
      });

      dragAndDrop.addTarget(new DragAndDrop.Target(fieldLabel) {
        @Override
        public boolean drag(Source source, Payload payload, float x, float y, int pointer) {
          if (!(source instanceof FilterUI.FieldBankSource)) {
            return false;
          }
          return true;
        }

        @Override
        public void drop(Source source, Payload payload, float x, float y, int pointer) {
          FieldPayload s = FieldPayload.class.cast(payload);
          SearchBuilderBox.this.sortField = s.getField();
          fieldLabel.setText(s.getField().displayName());
        }
      });

      sortOnBox.addActor(new G24Label("Sort on:", "title"));
      sortOnBox.addActor(fieldLabel);
      sortOnBox.addActor(descCheck);
      sortOnBox.addActor(ascCheck);

      addActor(new FilterEntry());
      addActor(addFilterButton);
      addActor(filterComboBox);
      addActor(sortOnBox);

      FilterUI.this.pack();
    }

    public void addEmptyFilterEntry() {
      for (Actor a : this.getChildren()) {
        if (a instanceof FilterEntry) {
          if (!FilterEntry.class.cast(a).isComplete()) {
            Notifier.warn("Complete previous filter", a);
            return;
          }
        }
      }
      addActorBefore(addFilterButton, new FilterEntry());
    }

    public Filters getFilters() {
      Array<Filter> ret = new Array<>();

      for (Actor a : this.getChildren()) {
        if (!(a instanceof FilterEntry)) {
          continue;
        }
        FilterEntry e = FilterEntry.class.cast(a);
        if (!e.isComplete()) {
          Notifier.warn("Filter is not complete", e);
          continue;
        }

        Filter f = null;
        FilterConstType t = e.field.get().getConstType();

        switch (t) {
          case Integer:

            Integer i = 0;
            try {
              i = Integer.parseInt(e.valueInput.getText().trim());
            } catch (NumberFormatException nfe) {
              Notifier.warn("Couldn't parse number from: \"" + e.valueInput.getText() + "\", using 0");
            }
            f = new Filter(e.field.get().toString(), e.method.get(), i);
            break;
          case String:
            f = new Filter(e.field.get().toString(), e.method.get(), e.valueInput.getText());
            break;
          default:
            break;
        }

        if (f != null) {
          ret.add(f);
        }

      }

      return new Filters(combination, ret.toArray(Filter.class));
    }
  }

  private class FieldSourceLabel extends G24Label {
    FieldSourceLabel(IsField field) {
      super(field.displayName(), "bubble");
      dragAndDrop.addSource(new FieldBankSource(this, field));
    }
  }

  private class MethodSourceLabel extends G24Label {
    MethodSourceLabel(Filter.Method method) {
      super(filterDisplayString(method), "bubble");
      dragAndDrop.addSource(new MethodBankSource(this, method));
    }
  }

  private VerticalGroup methodSources = new VerticalGroup();
  private VerticalGroup fieldSources = new VerticalGroup();
  private SearchBuilderBox box;

  private class ReturnBin extends DragAndDrop.Target {
    ReturnBin(Actor a) {
      super(a);
    }

    @Override
    public boolean drag(Source source, Payload payload, float x, float y, int pointer) {
      return true;
    }

    @Override
    public void drop(Source source, Payload payload, float x, float y, int pointer) {
    }
  }

  public Sort getSort() {
    return new Sort(box.sortField.toString(), box.sortDir);
  }

  public Filters getFilters() {
    return box.getFilters();
  }

  private static class FieldPayload extends DragAndDrop.Payload {
    public FieldPayload(IsField field) {
      G24Label actor = new G24Label(field.displayName(), "bubble");
      actor.pack();
      setDragActor(actor);

      G24Label invalid = new G24Label(field.displayName(), "bubble");
      invalid.setColor(1, 0.8f, 0.8f, 0.8f);
      invalid.pack();
      setInvalidDragActor(invalid);

      G24Label valid = new G24Label(field.displayName(), "bubble");
      valid.setColor(0.8f, 1, 0.8f, 0.8f);
      valid.pack();
      setValidDragActor(valid);

      setObject(field);
    }

    public IsField getField() {
      return IsField.class.cast(getObject());
    }
  }

  private static class MethodPayload extends DragAndDrop.Payload {
    public MethodPayload(Filter.Method method) {

      String s = filterDisplayString(method);
      G24Label actor = new G24Label(s, "bubble");
      actor.pack();
      setDragActor(actor);

      G24Label invalid = new G24Label(s, "bubble");
      invalid.setColor(1, 0.8f, 0.8f, 0.8f);
      invalid.pack();
      setInvalidDragActor(invalid);

      G24Label valid = new G24Label(s, "bubble");
      valid.setColor(0.8f, 1, 0.8f, 0.8f);
      valid.pack();
      setValidDragActor(valid);

      setObject(method);
    }

    public Filter.Method getMethod() {
      return Filter.Method.class.cast(getObject());
    }
  }
}
