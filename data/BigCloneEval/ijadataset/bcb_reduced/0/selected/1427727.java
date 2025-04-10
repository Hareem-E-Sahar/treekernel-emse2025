package hu.openig.screen.items;

import hu.openig.core.Act;
import hu.openig.core.Action1;
import hu.openig.model.Fleet;
import hu.openig.model.FleetKnowledge;
import hu.openig.model.FleetMode;
import hu.openig.model.FleetStatistics;
import hu.openig.model.InventoryItem;
import hu.openig.model.InventoryItemGroup;
import hu.openig.model.InventorySlot;
import hu.openig.model.Planet;
import hu.openig.model.PlanetStatistics;
import hu.openig.model.ResearchSubCategory;
import hu.openig.model.ResearchType;
import hu.openig.model.Screens;
import hu.openig.model.SelectionMode;
import hu.openig.model.SoundType;
import hu.openig.render.RenderTools;
import hu.openig.screen.EquipmentConfigure;
import hu.openig.screen.EquipmentMinimap;
import hu.openig.screen.FleetListing;
import hu.openig.screen.ScreenBase;
import hu.openig.screen.TechnologySlot;
import hu.openig.screen.VehicleCell;
import hu.openig.screen.VehicleList;
import hu.openig.ui.UIImage;
import hu.openig.ui.UIImageButton;
import hu.openig.ui.UIImageTabButton;
import hu.openig.ui.UILabel;
import hu.openig.ui.UIMouse;
import hu.openig.ui.UIMouse.Type;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * The equipment screen.
 * @author akarnokd, 2010.01.11.
 */
public class EquipmentScreen extends ScreenBase {

    /** The panel base rectangle. */
    final Rectangle base = new Rectangle();

    /** The left panel. */
    final Rectangle leftPanel = new Rectangle();

    /** The right panel. */
    final Rectangle rightPanel = new Rectangle();

    /** The info button. */
    UIImageButton infoButton;

    /** The production button. */
    UIImageButton productionButton;

    /** The research button. */
    UIImageButton researchButton;

    /** The bridge button. */
    UIImageButton bridgeButton;

    /** The large vertical starmap button right to the minimap. */
    UIImageButton starmapButton;

    /** The large vertical colony button right to the minimap. */
    UIImageButton colonyButton;

    /** The placeholder for the no-research-button case. */
    UIImage noResearch;

    /** The placeholder for the no-production-button case. */
    UIImage noProduction;

    /** Placeholder for the no-starmap-button case. */
    UIImage noStarmap;

    /** Placeholder for the no-colony-button case. */
    UIImage noColony;

    /** The previous button. */
    UIImageButton prev;

    /** The next button. */
    UIImageButton next;

    /** The fleet name. */
    UILabel fleetName;

    /** The spaceship count label. */
    UILabel spaceshipsLabel;

    /** The fighters count label. */
    UILabel fightersLabel;

    /** The vehicles count label. */
    UILabel vehiclesLabel;

    /** The spaceship count label. */
    UILabel spaceshipsMaxLabel;

    /** The fighters count label. */
    UILabel fightersMaxLabel;

    /** The vehicles count label. */
    UILabel vehiclesMaxLabel;

    /** The fleet status label. */
    UILabel fleetStatusLabel;

    /** Secondary label. */
    UILabel secondaryLabel;

    /** Secondary value.*/
    UILabel secondaryValue;

    /** Secondary fighters. */
    UILabel secondaryFighters;

    /** Secondary vehicles. */
    UILabel secondaryVehicles;

    /** Empty category image. */
    UIImage battleshipsAndStationsEmpty;

    /** Empty category  image. */
    UIImage cruisersEmpty;

    /** Empty category  image. */
    UIImage fightersEmpty;

    /** Empty category  image. */
    UIImage tanksEmpty;

    /** Empty category  image. */
    UIImage vehiclesEmpty;

    /** Battleships category. */
    UIImageTabButton battleships;

    /** Cruisers category. */
    UIImageTabButton cruisers;

    /** Fighters category. */
    UIImageTabButton fighters;

    /** Stations category. */
    UIImageTabButton stations;

    /** Tanks category. */
    UIImageTabButton tanks;

    /** Vehicles category. */
    UIImageTabButton vehicles;

    /** End splitting. */
    UIImageButton endSplit;

    /** End joining. */
    UIImageButton endJoin;

    /** No planet nearby error. */
    UIImage noPlanetNearby;

    /** No spaceport on the nearby planet. */
    UIImage noSpaceport;

    /** Not your planet. */
    UIImage notYourPlanet;

    /** New fleet button. */
    UIImageButton newButton;

    /** Add ship to fleet. */
    UIImageButton addButton;

    /** Delete ship to fleet. */
    UIImageButton delButton;

    /** Delete a fleet. */
    UIImageButton deleteButton;

    /** Transfer ships between fleets or planet. */
    UIImageButton transferButton;

    /** Split a fleet. */
    UIImageButton splitButton;

    /** Move one left. */
    UIImageButton left1;

    /** Move multiple left. */
    UIImageButton left2;

    /** Move all left. */
    UIImageButton left3;

    /** Move one right. */
    UIImageButton right1;

    /** Move multiple right.*/
    UIImageButton right2;

    /** Move all right.*/
    UIImageButton right3;

    /** Add one inner equipment. */
    UIImageButton addOne;

    /** Remove one inner equipment. */
    UIImageButton removeOne;

    /** The fleet listing button. */
    UIImageButton listButton;

    /** Inner equipment rectangle. */
    Rectangle innerEquipment;

    /** Show the inner equipment rectangle? */
    boolean innerEquipmentVisible;

    /** Inner equipment name. */
    UILabel innerEquipmentName;

    /** Inner equipment value. */
    UILabel innerEquipmentValue;

    /** Inner equipment separator. */
    UILabel innerEquipmentSeparator;

    /** The name and type of the current selected ship or equipment. */
    UILabel selectedNameAndType;

    /** The planet image. */
    UIImage planet;

    /** The equipment slot locations. */
    final List<TechnologySlot> slots = new ArrayList<TechnologySlot>();

    /** The rolling disk animation timer. */
    Closeable animation;

    /** The current animation step counter. */
    int animationStep;

    /** The left fighter cells. */
    final List<VehicleCell> leftFighterCells = new ArrayList<VehicleCell>();

    /** The left tank cells. */
    final List<VehicleCell> leftTankCells = new ArrayList<VehicleCell>();

    /** The right fighter cells. */
    final List<VehicleCell> rightFighterCells = new ArrayList<VehicleCell>();

    /** The right tank cells. */
    final List<VehicleCell> rightTankCells = new ArrayList<VehicleCell>();

    /** The left vehicle listing. */
    VehicleList leftList;

    /** The right vehicle listing. */
    VehicleList rightList;

    /** The currently displayed planet. */
    Planet planetShown;

    /** The currently displayed fleet. */
    Fleet fleetShown;

    /** The configuration component. */
    EquipmentConfigure configure;

    /** The fleet listing. */
    FleetListing fleetListing;

    /** The equipment minimap. */
    EquipmentMinimap minimap;

    /** Edit the primary name. */
    public boolean editPrimary;

    /** Edit the secondary name. */
    public boolean editSecondary;

    /** Signal the editing of new primary name. */
    public boolean editNew;

    /** Are we in transfer mode? */
    public boolean transferMode;

    /** The secondary fleet. */
    Fleet secondary;

    /** The random placement. */
    public final Random rnd = new Random();

    /** Sell the selected equipment. */
    UIImageButton sell;

    /** The last selection mode. */
    SelectionMode lastSelection;

    @Override
    public void onInitialize() {
        base.setBounds(0, 0, commons.equipment().base.getWidth(), commons.equipment().base.getHeight());
        infoButton = new UIImageButton(commons.common().infoButton);
        bridgeButton = new UIImageButton(commons.common().bridgeButton);
        researchButton = new UIImageButton(commons.research().research);
        productionButton = new UIImageButton(commons.research().production);
        researchButton.onClick = new Act() {

            @Override
            public void act() {
                displaySecondary(Screens.RESEARCH);
            }
        };
        productionButton.onClick = new Act() {

            @Override
            public void act() {
                displaySecondary(Screens.PRODUCTION);
            }
        };
        bridgeButton.onClick = new Act() {

            @Override
            public void act() {
                displayPrimary(Screens.BRIDGE);
            }
        };
        infoButton.onClick = new Act() {

            @Override
            public void act() {
                displaySecondary(Screens.INFORMATION_INVENTIONS);
            }
        };
        starmapButton = new UIImageButton(commons.equipment().starmap);
        starmapButton.onClick = new Act() {

            @Override
            public void act() {
                displayPrimary(Screens.STARMAP);
            }
        };
        colonyButton = new UIImageButton(commons.equipment().planet);
        colonyButton.onClick = new Act() {

            @Override
            public void act() {
                displayPrimary(Screens.COLONY);
            }
        };
        noResearch = new UIImage(commons.common().emptyButton);
        noResearch.visible(false);
        noProduction = new UIImage(commons.common().emptyButton);
        noProduction.visible(false);
        noStarmap = new UIImage(commons.equipment().buttonMapEmpty);
        noStarmap.visible(false);
        noColony = new UIImage(commons.equipment().buttonMapEmpty);
        noColony.visible(false);
        endSplit = new UIImageButton(commons.equipment().endSplit);
        endSplit.visible(false);
        endSplit.onClick = new Act() {

            @Override
            public void act() {
                doEndSplit();
            }
        };
        endJoin = new UIImageButton(commons.equipment().endJoin);
        endJoin.visible(false);
        endJoin.onClick = new Act() {

            @Override
            public void act() {
                doEndJoin();
            }
        };
        prev = new UIImageButton(commons.starmap().backwards);
        prev.onClick = new Act() {

            @Override
            public void act() {
                doPrev();
            }
        };
        prev.setDisabledPattern(commons.common().disabledPattern);
        next = new UIImageButton(commons.starmap().forwards);
        next.onClick = new Act() {

            @Override
            public void act() {
                doNext();
            }
        };
        next.setDisabledPattern(commons.common().disabledPattern);
        fleetName = new UILabel("Fleet1", 14, commons.text()) {

            @Override
            public boolean mouse(UIMouse e) {
                if (e.has(Type.DOUBLE_CLICK) && player().selectionMode == SelectionMode.FLEET && fleet() != null) {
                    editPrimary = true;
                    editSecondary = false;
                }
                return super.mouse(e);
            }
        };
        fleetName.color(0xFFFF0000);
        spaceshipsLabel = new UILabel(format("equipment.spaceships", 0), 10, commons.text());
        fightersLabel = new UILabel(format("equipment.fighters", 0), 10, commons.text());
        vehiclesLabel = new UILabel(format("equipment.vehicles", 0), 10, commons.text());
        spaceshipsMaxLabel = new UILabel(format("equipment.max", 25), 10, commons.text());
        fightersMaxLabel = new UILabel(format("equipment.maxpertype", 30), 10, commons.text());
        vehiclesMaxLabel = new UILabel(format("equipment.max", 0), 10, commons.text());
        fleetStatusLabel = new UILabel("TODO", 10, commons.text());
        secondaryLabel = new UILabel(get("equipment.secondary"), 10, commons.text()) {

            @Override
            public boolean mouse(UIMouse e) {
                if (e.has(Type.DOUBLE_CLICK) && player().selectionMode == SelectionMode.FLEET && secondary != null) {
                    editPrimary = false;
                    editSecondary = true;
                }
                return super.mouse(e);
            }
        };
        secondaryValue = new UILabel("TODO", 10, commons.text()) {

            @Override
            public boolean mouse(UIMouse e) {
                if (e.has(Type.DOUBLE_CLICK) && player().selectionMode == SelectionMode.FLEET && secondary != null) {
                    editPrimary = false;
                    editSecondary = true;
                }
                return super.mouse(e);
            }
        };
        secondaryValue.color(0xFFFF0000);
        secondaryFighters = new UILabel(format("equipment.fighters", 0), 10, commons.text());
        secondaryVehicles = new UILabel(format("equipment.vehiclesandmax", 0, 8), 10, commons.text());
        battleshipsAndStationsEmpty = new UIImage(commons.equipment().categoryEmpty);
        battleshipsAndStationsEmpty.visible(false);
        cruisersEmpty = new UIImage(commons.equipment().categoryEmpty);
        cruisersEmpty.visible(false);
        fightersEmpty = new UIImage(commons.equipment().categoryEmpty);
        fightersEmpty.visible(false);
        tanksEmpty = new UIImage(commons.equipment().categoryEmpty);
        tanksEmpty.visible(false);
        vehiclesEmpty = new UIImage(commons.equipment().categoryEmpty);
        vehiclesEmpty.visible(false);
        battleships = new UIImageTabButton(commons.equipment().categoryBattleships);
        cruisers = new UIImageTabButton(commons.equipment().categoryCruisers);
        fighters = new UIImageTabButton(commons.equipment().categoryFighers);
        stations = new UIImageTabButton(commons.equipment().categorySpaceStations);
        stations.visible(false);
        tanks = new UIImageTabButton(commons.equipment().categoryTanks);
        vehicles = new UIImageTabButton(commons.equipment().categoryVehicles);
        battleships.onPress = categoryAction(ResearchSubCategory.SPACESHIPS_BATTLESHIPS);
        cruisers.onPress = categoryAction(ResearchSubCategory.SPACESHIPS_CRUISERS);
        fighters.onPress = categoryAction(ResearchSubCategory.SPACESHIPS_FIGHTERS);
        stations.onPress = categoryAction(ResearchSubCategory.SPACESHIPS_STATIONS);
        tanks.onPress = categoryAction(ResearchSubCategory.WEAPONS_TANKS);
        vehicles.onPress = categoryAction(ResearchSubCategory.WEAPONS_VEHICLES);
        slots.clear();
        noPlanetNearby = new UIImage(commons.equipment().noPlanetNearby);
        noPlanetNearby.visible(false);
        noSpaceport = new UIImage(commons.equipment().noSpaceport);
        noSpaceport.visible(false);
        notYourPlanet = new UIImage(commons.equipment().notYourplanet);
        notYourPlanet.visible(false);
        newButton = new UIImageButton(commons.equipment().newFleet);
        newButton.visible(false);
        newButton.onClick = new Act() {

            @Override
            public void act() {
                if (player().selectionMode == SelectionMode.PLANET) {
                    doCreateFleet(true, planet().x, planet().y);
                } else {
                    if (fleet() != null) {
                        FleetStatistics fs = fleet().getStatistics();
                        if (fs.planet != null && fs.planet.owner == player()) {
                            doCreateFleet(true, fs.planet.x, fs.planet.y);
                        }
                    }
                }
                editNew = true;
                if (config.computerVoiceScreen) {
                    commons.sounds.play(SoundType.NEW_FLEET);
                }
            }
        };
        addButton = new UIImageButton(commons.equipment().add);
        addButton.visible(false);
        addButton.onClick = new Act() {

            @Override
            public void act() {
                if (addButton.visible()) {
                    doAddItem();
                }
            }
        };
        addButton.setHoldDelay(150);
        delButton = new UIImageButton(commons.equipment().remove);
        delButton.visible(false);
        delButton.onClick = new Act() {

            @Override
            public void act() {
                if (delButton.visible()) {
                    doRemoveItem();
                }
            }
        };
        delButton.setHoldDelay(150);
        deleteButton = new UIImageButton(commons.equipment().delete);
        deleteButton.visible(false);
        deleteButton.onClick = new Act() {

            @Override
            public void act() {
                doDeleteFleet();
            }
        };
        transferButton = new UIImageButton(commons.equipment().transfer);
        transferButton.visible(false);
        transferButton.onClick = new Act() {

            @Override
            public void act() {
                doTransfer();
                if (config.computerVoiceScreen) {
                    commons.sounds.play(SoundType.JOIN_FLEETS);
                }
            }
        };
        splitButton = new UIImageButton(commons.equipment().split);
        splitButton.visible(false);
        splitButton.onClick = new Act() {

            @Override
            public void act() {
                doSplit();
                if (config.computerVoiceScreen) {
                    commons.sounds.play(SoundType.SPLIT_FLEET);
                }
            }
        };
        addOne = new UIImageButton(commons.equipment().addOne);
        addOne.visible(false);
        addOne.onClick = new Act() {

            @Override
            public void act() {
                if (addOne.visible()) {
                    doAddOne();
                }
            }
        };
        addOne.setHoldDelay(150);
        removeOne = new UIImageButton(commons.equipment().removeOne);
        removeOne.visible(false);
        removeOne.onClick = new Act() {

            @Override
            public void act() {
                if (removeOne.visible()) {
                    doRemoveOne();
                }
            }
        };
        removeOne.setHoldDelay(150);
        left1 = new UIImageButton(commons.equipment().moveLeft1);
        left1.visible(false);
        left1.onClick = new Act() {

            @Override
            public void act() {
                doMoveItem(secondary, fleet(), research(), 1, rightList.groupIndex(research(), 0));
            }
        };
        left2 = new UIImageButton(commons.equipment().moveLeft2);
        left2.visible(false);
        left2.onClick = new Act() {

            @Override
            public void act() {
                doMoveItem(secondary, fleet(), research(), 2, rightList.groupIndex(research(), 0));
            }
        };
        left3 = new UIImageButton(commons.equipment().moveLeft3);
        left3.visible(false);
        left3.onClick = new Act() {

            @Override
            public void act() {
                doMoveItem(secondary, fleet(), research(), 3, rightList.groupIndex(research(), 0));
            }
        };
        right1 = new UIImageButton(commons.equipment().moveRight1);
        right1.visible(false);
        right1.onClick = new Act() {

            @Override
            public void act() {
                doMoveItem(fleet(), secondary, research(), 1, leftList.groupIndex(research(), 0));
            }
        };
        right2 = new UIImageButton(commons.equipment().moveRight2);
        right2.visible(false);
        right2.onClick = new Act() {

            @Override
            public void act() {
                doMoveItem(fleet(), secondary, research(), 2, leftList.groupIndex(research(), 0));
            }
        };
        right3 = new UIImageButton(commons.equipment().moveRight3);
        right3.visible(false);
        right3.onClick = new Act() {

            @Override
            public void act() {
                doMoveItem(fleet(), secondary, research(), 3, leftList.groupIndex(research(), 0));
            }
        };
        listButton = new UIImageButton(commons.equipment().list);
        listButton.onClick = new Act() {

            @Override
            public void act() {
                fleetListing.visible(!fleetListing.visible());
                if (fleetListing.visible()) {
                    if (transferMode) {
                        fleetListing.show(secondary);
                    } else {
                        fleetListing.show(fleet());
                    }
                }
            }
        };
        innerEquipment = new Rectangle();
        innerEquipmentName = new UILabel("TODO", 7, commons.text());
        innerEquipmentName.visible(false);
        innerEquipmentValue = new UILabel(format("equipment.innercount", 0, 0), 7, commons.text());
        innerEquipmentValue.visible(false);
        innerEquipmentSeparator = new UILabel("-----", 7, commons.text());
        innerEquipmentSeparator.visible(false);
        selectedNameAndType = new UILabel(format("equipment.selectednametype", "TODO", "TODO"), 10, commons.text());
        selectedNameAndType.visible(false);
        selectedNameAndType.color(0xFF6DB269);
        planet = new UIImage(commons.equipment().planetOrbit);
        leftList = new VehicleList(commons);
        rightList = new VehicleList(commons);
        rightList.visible(false);
        configure = new EquipmentConfigure();
        configure.z = -1;
        configure.size(298, 128);
        configure.onSelect = new Action1<InventorySlot>() {

            @Override
            public void invoke(InventorySlot value) {
                onSelectInventorySlot(value);
            }
        };
        Action1<ResearchType> selectSlot = new Action1<ResearchType>() {

            @Override
            public void invoke(ResearchType value) {
                onSelectResearchSlot(value);
            }
        };
        Action1<ResearchType> selectVehicle = new Action1<ResearchType>() {

            @Override
            public void invoke(ResearchType value) {
                onSelectVehicleSlot(value);
            }
        };
        leftList.onSelect = new Action1<InventoryItem>() {

            @Override
            public void invoke(InventoryItem value) {
                onSelectInventoryItem(value);
            }
        };
        rightList.onSelect = new Action1<InventoryItem>() {

            @Override
            public void invoke(InventoryItem value) {
                onSelectInventoryItem(value);
            }
        };
        for (int i = 0; i < 6; i++) {
            final TechnologySlot ts = new TechnologySlot(commons);
            ts.visible(false);
            ts.onPress = selectSlot;
            slots.add(ts);
        }
        for (int i = 0; i < 6; i++) {
            VehicleCell vc = new VehicleCell(commons);
            vc.onSelect = selectVehicle;
            vc.topCenter = true;
            leftFighterCells.add(vc);
            vc = new VehicleCell(commons);
            vc.topCenter = true;
            vc.onSelect = selectVehicle;
            rightFighterCells.add(vc);
        }
        for (int i = 0; i < 7; i++) {
            VehicleCell vc = new VehicleCell(commons);
            vc.onSelect = selectVehicle;
            leftTankCells.add(vc);
            vc = new VehicleCell(commons);
            vc.onSelect = selectVehicle;
            rightTankCells.add(vc);
        }
        fleetListing = new FleetListing(commons);
        fleetListing.z = 2;
        fleetListing.visible(false);
        fleetListing.onSelect = new Action1<Fleet>() {

            @Override
            public void invoke(Fleet value) {
                if (!transferMode) {
                    player().currentFleet = value;
                    player().selectionMode = SelectionMode.FLEET;
                } else {
                    secondary = value;
                    fleetListing.visible(false);
                    updateInventory(null, secondary, rightList);
                }
                configure.type = research();
                configure.item = fleet().getInventoryItem(research());
                configure.selectedSlot = null;
                doSelectListVehicle(research());
                doSelectVehicle(research());
            }
        };
        minimap = new EquipmentMinimap(commons);
        sell = new UIImageButton(commons.equipment().sell);
        sell.onClick = new Act() {

            @Override
            public void act() {
                doSell();
            }
        };
        add(leftFighterCells);
        add(rightFighterCells);
        add(leftTankCells);
        add(rightTankCells);
        add(slots);
        addThis();
    }

    /**
	 * @param cat the category to set 
	 * @return Create an action which selects the given category. 
	 */
    Act categoryAction(final ResearchSubCategory cat) {
        return new Act() {

            @Override
            public void act() {
                displayCategory(cat);
                configure.selectedSlot = null;
            }
        };
    }

    @Override
    public void onEnter(Screens mode) {
        ResearchType rt = research();
        if (rt == null) {
            List<ResearchType> rts = world().getResearch();
            if (rts.size() > 0) {
                rt = rts.get(0);
                world().selectResearch(rt);
            }
        }
        displayCategory(rt.category);
        doSelectVehicle(rt);
        doSelectListVehicle(rt);
        updateCurrentInventory();
        configure.type = rt;
        configure.item = leftList.selectedItem;
        configure.selectedSlot = null;
        if (player().selectionMode == SelectionMode.FLEET && fleet() != null) {
            minimap.moveTo(fleet().x, fleet().y);
        } else if (player().selectionMode == SelectionMode.PLANET || player().selectionMode == null) {
            minimap.moveTo(planet().x, planet().y);
        }
        editPrimary = false;
        editSecondary = false;
        editNew = false;
        fleetListing.nearby = false;
        secondary = null;
        animation = commons.register(100, new Act() {

            @Override
            public void act() {
                doAnimation();
            }
        });
    }

    @Override
    public void onLeave() {
        close0(animation);
        animation = null;
        fleetListing.visible(false);
        for (Fleet f : new ArrayList<Fleet>(player().fleets.keySet())) {
            if (f.inventory.isEmpty()) {
                player().fleets.remove(f);
            }
        }
        if (fleet() != null && fleet().inventory.isEmpty()) {
            if (player().fleets.isEmpty()) {
                player().currentFleet = null;
                player().selectionMode = SelectionMode.PLANET;
            } else {
                player().currentFleet = player().fleets.keySet().iterator().next();
            }
        }
    }

    @Override
    public void onFinish() {
    }

    @Override
    public void onResize() {
        RenderTools.centerScreen(base, width, height, true);
        leftPanel.setBounds(base.x + 1, base.y + 1, 318, 198);
        rightPanel.setBounds(leftPanel.x + leftPanel.width + 2, leftPanel.y, 318, 198);
        infoButton.location(base.x + 535, base.y + 303 - 20);
        productionButton.location(infoButton.x, infoButton.y + infoButton.height);
        researchButton.location(productionButton.x, productionButton.y + productionButton.height);
        bridgeButton.location(researchButton.x, researchButton.y + researchButton.height);
        starmapButton.location(base.x + 479, base.y + 303 - 20);
        colonyButton.location(starmapButton.x + 26, starmapButton.y);
        noResearch.location(researchButton.location());
        noProduction.location(productionButton.location());
        noStarmap.location(starmapButton.location());
        noColony.location(colonyButton.location());
        endJoin.location(infoButton.location());
        endSplit.location(infoButton.location());
        prev.location(base.x + 151, base.y + 304 - 20);
        next.location(base.x + 152 + 50, base.y + 304 - 20);
        fleetName.location(base.x + 3, base.y + 308 - 20);
        fleetName.width = 147;
        spaceshipsLabel.location(fleetName.x + 3, fleetName.y + 20);
        fightersLabel.location(spaceshipsLabel.x, spaceshipsLabel.y + 14);
        vehiclesLabel.location(fightersLabel.x, fightersLabel.y + 14);
        fleetStatusLabel.location(vehiclesLabel.x, vehiclesLabel.y + 14);
        spaceshipsMaxLabel.location(spaceshipsLabel.x + 110, spaceshipsLabel.y);
        fightersMaxLabel.location(fightersLabel.x + 110, fightersLabel.y);
        vehiclesMaxLabel.location(vehiclesLabel.x + 110, vehiclesLabel.y);
        secondaryLabel.location(fightersLabel.x, fleetStatusLabel.y + 22);
        secondaryValue.location(secondaryLabel.x + secondaryLabel.width + 8, secondaryLabel.y);
        secondaryValue.width = base.x + 250 - secondaryValue.x;
        secondaryFighters.location(secondaryLabel.x, secondaryLabel.y + 14);
        secondaryVehicles.location(secondaryLabel.x, secondaryFighters.y + 14);
        battleships.location(base.x + 2, base.y + 435 - 20);
        stations.location(battleships.location());
        cruisers.location(battleships.x + 50, battleships.y);
        fighters.location(cruisers.x + 50, cruisers.y);
        tanks.location(fighters.x + 50, fighters.y);
        vehicles.location(tanks.x + 50, tanks.y);
        battleshipsAndStationsEmpty.location(battleships.location());
        cruisersEmpty.location(cruisers.location());
        fightersEmpty.location(fighters.location());
        tanksEmpty.location(tanks.location());
        vehiclesEmpty.location(vehicles.location());
        for (int i = 0; i < 6; i++) {
            slots.get(i).location(base.x + 2 + i * 106, base.y + 219 - 20);
            slots.get(i).size(106, 82);
        }
        noPlanetNearby.location(base.x + 242, base.y + 194 - 20);
        noSpaceport.location(noPlanetNearby.location());
        notYourPlanet.location(noSpaceport.location());
        transferButton.location(base.x + 401, base.y + 194 - 20);
        splitButton.location(base.x + 480, base.y + 194 - 20);
        newButton.location(base.x + 560, base.y + 194 - 20);
        addButton.location(base.x + 322, base.y + 194 - 20);
        addOne.location(addButton.location());
        delButton.location(base.x + 242, base.y + 194 - 20);
        removeOne.location(base.x + 242, base.y + 194 - 20);
        listButton.location(base.x + 620, base.y + 49 - 20);
        right1.location(base.x + 322, base.y + 191 - 20);
        right2.location(right1.x + 48, right1.y);
        right3.location(right2.x + 48, right2.y);
        left1.location(base.x + 272, base.y + 191 - 20);
        left2.location(left1.x - 48, left1.y);
        left3.location(left2.x - 48, left2.y);
        innerEquipment.setBounds(base.x + 325, base.y + 156 - 20, 120, 35);
        innerEquipmentName.location(innerEquipment.x + 5, innerEquipment.y + 4);
        innerEquipmentSeparator.location(innerEquipmentName.x, innerEquipmentName.y + 10);
        innerEquipmentValue.location(innerEquipmentName.x, innerEquipmentName.y + 20);
        selectedNameAndType.location(base.x + 326, base.y + 56 - 20);
        planet.location(leftPanel.x - 1, leftPanel.y - 1);
        for (int i = 0; i < 6; i++) {
            VehicleCell vc = leftFighterCells.get(i);
            vc.bounds(leftPanel.x + leftPanel.width - 2 - (6 - i) * 34, leftPanel.y + 2, 33, 38);
            vc = rightFighterCells.get(i);
            vc.bounds(rightPanel.x + 2 + i * 34, rightPanel.y + 2, 33, 38);
        }
        for (int i = 0; i < 7; i++) {
            VehicleCell vc = leftTankCells.get(i);
            vc.bounds(leftPanel.x + leftPanel.width - 2 - (7 - i) * 34, leftPanel.y + leftPanel.height - 56, 33, 28);
            vc = rightTankCells.get(i);
            vc.bounds(rightPanel.x + 2 + i * 34, rightPanel.y + rightPanel.height - 56, 33, 28);
        }
        leftList.bounds(leftPanel.x + 72, leftPanel.y + 40, leftPanel.width - 74, leftPanel.height - 56 - 38);
        rightList.bounds(rightPanel.x, rightPanel.y + 40, rightPanel.width - 22, rightPanel.height - 56 - 38);
        configure.bounds(rightPanel.x, rightPanel.y + 28, 298, 128);
        fleetListing.bounds(rightPanel.x + 5, listButton.y, rightPanel.width - 26, listButton.height);
        minimap.bounds(base.x + 254, base.y + 281, 225, 161);
        sell.location(delButton.x - delButton.width - 2, delButton.y);
        deleteButton.location(sell.x - sell.width, sell.y);
    }

    @Override
    public void draw(Graphics2D g2) {
        RenderTools.darkenAround(base, width, height, g2, 0.5f, true);
        g2.drawImage(commons.equipment().base, base.x, base.y, null);
        update();
        super.draw(g2);
        if (innerEquipmentVisible) {
            g2.setColor(new Color(0xFF4D7DB6));
            g2.drawRect(innerEquipment.x, innerEquipment.y, innerEquipment.width - 1, innerEquipment.height - 1);
        }
    }

    /**
	 * Update the slot belonging to the specified technology.
	 * @param rt the research technology
	 */
    public void updateSlot(final ResearchType rt) {
        final TechnologySlot slot = slots.get(rt.index);
        slot.type = rt;
        slot.displayResearchCost = false;
        slot.displayProductionCost = false;
        slot.visible(true);
    }

    /**
	 * Update animating components.
	 */
    void doAnimation() {
        if (animationStep == Integer.MAX_VALUE) {
            animationStep = 0;
        } else {
            animationStep++;
        }
        for (TechnologySlot sl : slots) {
            sl.animationStep = animationStep;
        }
        askRepaint(base);
    }

    @Override
    public boolean mouse(UIMouse e) {
        if (!base.contains(e.x, e.y) && e.has(Type.UP) && !minimap.panMode) {
            hideSecondary();
            return true;
        } else {
            if (e.has(Type.DOWN)) {
                if (!e.within(fleetListing.x, fleetListing.y, fleetListing.width, fleetListing.height) && !e.within(listButton.x, listButton.y, listButton.width, listButton.height)) {
                    fleetListing.visible(false);
                }
            }
            return super.mouse(e);
        }
    }

    @Override
    public Screens screen() {
        return Screens.EQUIPMENT;
    }

    @Override
    public void onEndGame() {
        for (TechnologySlot ts : slots) {
            ts.type = null;
        }
        clearCells(leftFighterCells);
        clearCells(rightFighterCells);
        clearCells(leftTankCells);
        clearCells(rightTankCells);
        leftList.clear();
        rightList.clear();
        leftList.selectedItem = null;
        rightList.selectedItem = null;
        secondary = null;
        fleetShown = null;
        planetShown = null;
    }

    /** 
	 * Create a new fleet. 
	 * @param select the new fleet?
	 * @param whereX where to create
	 * @param whereY where to create
	 * @return the new fleet
	 */
    Fleet doCreateFleet(boolean select, float whereX, float whereY) {
        Fleet f = new Fleet();
        f.id = world().fleetIdSequence++;
        f.owner = player();
        f.name = get("newfleet.name");
        int r = rnd.nextInt(14) + 5;
        double k = rnd.nextDouble() * 2 * Math.PI;
        f.x = (float) (whereX + Math.cos(k) * r);
        f.y = (float) (whereY + Math.sin(k) * r);
        if (select) {
            player().currentFleet = f;
            player().selectionMode = SelectionMode.FLEET;
        }
        player().fleets.put(f, FleetKnowledge.FULL);
        return f;
    }

    /**
	 * Update the display values based on the current selection.
	 */
    void update() {
        innerEquipmentVisible = false;
        innerEquipmentName.visible(false);
        innerEquipmentSeparator.visible(false);
        innerEquipmentValue.visible(false);
        ResearchType rt = research();
        if (player().selectionMode == SelectionMode.PLANET) {
            PlanetStatistics ps = planet().getStatistics();
            newButton.visible(planet().owner == player() && ps.hasMilitarySpaceport);
            noSpaceport.visible(planet().owner == player() && !ps.hasMilitarySpaceport);
            notYourPlanet.visible(planet().owner != player());
            noPlanetNearby.visible(false);
            if (planetShown != planet() || lastSelection != player().selectionMode) {
                planetShown = planet();
                lastSelection = player().selectionMode;
                updateCurrentInventory();
                minimap.moveTo(planet().x, planet().y);
            }
            planet.visible(true);
            List<Planet> planets = player().ownPlanets();
            Collections.sort(planets, Planet.NAME_ORDER);
            int idx = planets.indexOf(planet());
            prev.enabled(idx > 0);
            next.enabled(idx < planets.size() - 1);
            fleetName.text(planet().name);
            spaceshipsLabel.visible(false);
            spaceshipsMaxLabel.visible(false);
            fightersLabel.text(format("equipment.fighters", ps.fighterCount), true);
            vehiclesLabel.text(format("equipment.vehicles", ps.vehicleCount), true);
            if (ps.hasSpaceStation) {
                fightersMaxLabel.text(format("equipment.maxpertype", 30), true);
            } else {
                fightersMaxLabel.text(format("equipment.max", 0), true);
            }
            vehiclesMaxLabel.text(format("equipment.max", ps.vehicleMax), true);
            fleetStatusLabel.visible(false);
            secondaryLabel.visible(false);
            secondaryFighters.visible(false);
            secondaryVehicles.visible(false);
            secondaryValue.visible(false);
            prepareCells(planet(), null, leftFighterCells, leftTankCells);
            clearCells(rightFighterCells);
            clearCells(rightTankCells);
            battleships.visible(false);
            cruisers.visible(false);
            cruisersEmpty.visible(true);
            stations.visible(true);
            configure.selectedSlot = null;
            if (rt.category == ResearchSubCategory.SPACESHIPS_STATIONS) {
                addButton.visible(player().inventoryCount(rt) > 0 && planet().inventoryCount(rt.category, player()) < 3);
                delButton.visible(false);
                sell.visible(planet().inventoryCount(rt, player()) > 0);
            } else if (ps.hasMilitarySpaceport && rt.category == ResearchSubCategory.SPACESHIPS_FIGHTERS) {
                addButton.visible(ps.hasSpaceStation && player().inventoryCount(rt) > 0 && planet().inventoryCount(rt, player()) < 30);
                delButton.visible(planet().inventoryCount(rt, player()) > 0);
                sell.visible(delButton.visible());
            } else if (rt.category == ResearchSubCategory.WEAPONS_TANKS || rt.category == ResearchSubCategory.WEAPONS_VEHICLES) {
                addButton.visible(player().inventoryCount(rt) > 0 && planet().inventoryCount(rt.category, player()) < ps.vehicleMax);
                delButton.visible(planet().inventoryCount(rt, player()) > 0);
                sell.visible(delButton.visible());
            } else {
                addButton.visible(false);
                delButton.visible(false);
                sell.visible(false);
            }
            addOne.visible(false);
            removeOne.visible(false);
            editPrimary = false;
            editSecondary = false;
            transferMode = false;
            secondary = null;
            configure.item = null;
            configure.selectedSlot = null;
            deleteButton.visible(false);
            splitButton.visible(false);
            transferButton.visible(false);
        } else {
            if (fleetShown != fleet() || lastSelection != player().selectionMode) {
                fleetShown = fleet();
                lastSelection = player().selectionMode;
                updateCurrentInventory();
                minimap.moveTo(fleet().x, fleet().y);
                editPrimary = editNew;
                editNew = false;
                editSecondary = false;
                configure.type = research();
                configure.item = fleet().getInventoryItem(research());
                configure.selectedSlot = null;
                transferMode = false;
                secondary = null;
                doSelectVehicle(research());
                doSelectListVehicle(research());
            }
            Fleet f = fleet();
            FleetStatistics fs = f.getStatistics();
            prepareCells(null, fleet(), leftFighterCells, leftTankCells);
            PlanetStatistics ps = fs.planet != null ? fs.planet.getStatistics() : null;
            List<Fleet> fleets = player().ownFleets();
            int idx = fleets.indexOf(f);
            prev.enabled(idx > 0);
            next.enabled(idx < fleets.size() - 1);
            planet.visible(false);
            if (editPrimary && (animationStep % 10) < 5) {
                fleetName.text(f.name + "-");
            } else {
                fleetName.text(f.name);
            }
            spaceshipsLabel.text(format("equipment.spaceships", fs.cruiserCount), true).visible(true);
            spaceshipsMaxLabel.text(format("equipment.max", 25), true).visible(true);
            fightersLabel.text(format("equipment.fighters", fs.fighterCount), true);
            vehiclesLabel.text(format("equipment.vehicles", fs.vehicleCount), true);
            fightersMaxLabel.text(format("equipment.maxpertype", 30), true);
            vehiclesMaxLabel.text(format("equipment.max", fs.vehicleMax), true);
            if (secondary != null) {
                secondaryLabel.visible(true);
                if (editSecondary && (animationStep % 10) < 5) {
                    secondaryValue.text(secondary.name + "-").visible(true);
                } else {
                    secondaryValue.text(secondary.name).visible(true);
                }
                FleetStatistics fs2 = secondary.getStatistics();
                secondaryFighters.text(format("equipment.fighters", fs2.fighterCount)).visible(true);
                secondaryVehicles.text(format("equipment.vehiclesandmax", fs2.vehicleCount, fs2.vehicleMax)).visible(true);
                boolean bleft = false;
                boolean bright = false;
                if (rt.category == ResearchSubCategory.SPACESHIPS_FIGHTERS) {
                    bleft = fleet().inventoryCount(rt) < 30 && secondary.inventoryCount(rt) > 0;
                    bright = fleet().inventoryCount(rt) > 0 && secondary.inventoryCount(rt) < 30;
                } else if (rt.category == ResearchSubCategory.WEAPONS_TANKS || rt.category == ResearchSubCategory.WEAPONS_VEHICLES) {
                    bleft = fs.vehicleCount < fs.vehicleMax && secondary.inventoryCount(rt) > 0;
                    bright = fs2.vehicleCount < fs.vehicleMax && fleet().inventoryCount(rt) > 0;
                } else if (rt.category == ResearchSubCategory.SPACESHIPS_BATTLESHIPS) {
                    bleft = fs.battleshipCount < 3 && secondary.inventoryCount(rt) > 0;
                    bright = fleet().inventoryCount(rt) > 0 && fs.battleshipCount < 3;
                } else if (rt.category == ResearchSubCategory.SPACESHIPS_CRUISERS) {
                    bleft = fs.cruiserCount < 25 && secondary.inventoryCount(rt) > 0;
                    bright = fleet().inventoryCount(rt) > 0 && fs2.cruiserCount < 25;
                }
                left1.visible(bleft);
                left2.visible(bleft);
                left3.visible(bleft);
                right1.visible(bright);
                right2.visible(bright);
                right3.visible(bright);
                listButton.visible(transferMode);
                endSplit.visible(!transferMode);
                endJoin.visible(transferMode);
                infoButton.visible(false);
                configure.visible(false);
                rightList.visible(true);
                prepareCells(null, secondary, rightFighterCells, rightTankCells);
                transferButton.visible(false);
                splitButton.visible(false);
                deleteButton.visible(false);
            } else {
                infoButton.visible(true);
                endSplit.visible(false);
                endJoin.visible(false);
                secondaryLabel.visible(false);
                secondaryValue.visible(false);
                secondaryFighters.visible(false);
                secondaryVehicles.visible(false);
                editSecondary = false;
                listButton.visible(true);
                configure.visible(true);
                clearCells(rightFighterCells);
                clearCells(rightTankCells);
                left1.visible(false);
                left2.visible(false);
                left3.visible(false);
                right1.visible(false);
                right2.visible(false);
                right3.visible(false);
                rightList.visible(false);
            }
            fleetStatusLabel.visible(true);
            if (f.targetFleet == null && f.targetPlanet == null) {
                if (f.waypoints.size() > 0) {
                    fleetStatusLabel.text(format("fleetstatus.moving"), true);
                } else {
                    if (fs.planet != null) {
                        fleetStatusLabel.text(format("fleetstatus.stopped.at", fs.planet.name), true);
                    } else {
                        fleetStatusLabel.text(format("fleetstatus.stopped"), true);
                    }
                }
            } else {
                if (f.mode == FleetMode.ATTACK) {
                    if (f.targetFleet != null) {
                        fleetStatusLabel.text(format("fleetstatus.attack", f.targetFleet.name), true);
                    } else {
                        fleetStatusLabel.text(format("fleetstatus.attack", f.targetPlanet.name), true);
                    }
                } else {
                    if (f.targetFleet != null) {
                        fleetStatusLabel.text(format("fleetstatus.moving.after", f.targetFleet.name), true);
                    } else {
                        fleetStatusLabel.text(format("fleetstatus.moving.to", f.targetPlanet.name), true);
                    }
                }
            }
            battleships.visible(true);
            cruisers.visible(true);
            cruisersEmpty.visible(false);
            stations.visible(false);
            newButton.visible(secondary == null && ps != null && fs.planet.owner == f.owner && ps.hasMilitarySpaceport);
            noSpaceport.visible(secondary == null && ps != null && fs.planet.owner == f.owner && !ps.hasMilitarySpaceport);
            notYourPlanet.visible(secondary == null && ps != null && fs.planet.owner != f.owner);
            noPlanetNearby.visible(secondary == null && ps == null);
            if (ps != null && fs.planet.owner == f.owner && ps.hasMilitarySpaceport && secondary == null) {
                if (rt.category == ResearchSubCategory.SPACESHIPS_FIGHTERS) {
                    addButton.visible(player().inventoryCount(rt) > 0 && fleet().inventoryCount(rt) < 30);
                    delButton.visible(fleet().inventoryCount(rt) > 0);
                    sell.visible(fleet().inventoryCount(rt) > 0);
                } else if (rt.category == ResearchSubCategory.WEAPONS_TANKS || rt.category == ResearchSubCategory.WEAPONS_VEHICLES) {
                    addButton.visible(player().inventoryCount(rt) > 0 && fleet().inventoryCount(rt.category) < fs.vehicleMax);
                    delButton.visible(fleet().inventoryCount(rt) > 0);
                    sell.visible(fleet().inventoryCount(rt) > 0);
                } else if (rt.category == ResearchSubCategory.SPACESHIPS_BATTLESHIPS) {
                    addButton.visible(player().inventoryCount(rt) > 0 && fleet().inventoryCount(rt.category) < 3);
                    delButton.visible(false);
                    sell.visible(fleet().inventoryCount(rt) > 0);
                } else if (rt.category == ResearchSubCategory.SPACESHIPS_CRUISERS) {
                    addButton.visible(player().inventoryCount(rt) > 0 && fleet().inventoryCount(rt.category) < 25);
                    delButton.visible(false);
                    sell.visible(fleet().inventoryCount(rt) > 0);
                } else {
                    addButton.visible(false);
                    delButton.visible(false);
                    sell.visible(false);
                }
                if (configure.selectedSlot != null) {
                    addOne.visible(player().inventoryCount(rt) > 0 && (configure.selectedSlot.type != rt || !configure.selectedSlot.isFilled()));
                    removeOne.visible(configure.selectedSlot.type != null && configure.selectedSlot.count > 0);
                    innerEquipmentVisible = true;
                    innerEquipmentName.text(get("inventoryslot." + configure.selectedSlot.slot.id), true).visible(true);
                    if (configure.selectedSlot.type != null) {
                        innerEquipmentSeparator.text(configure.selectedSlot.type.name, true).visible(true);
                    } else {
                        innerEquipmentSeparator.text("----", true).visible(true);
                    }
                    innerEquipmentValue.text(format("equipment.innercount", configure.selectedSlot.count, configure.selectedSlot.slot.max), true).visible(true);
                } else {
                    addOne.visible(false);
                    removeOne.visible(false);
                }
            } else {
                addButton.visible(false);
                delButton.visible(false);
                addOne.visible(false);
                removeOne.visible(false);
                sell.visible(false);
            }
            splitButton.visible(secondary == null && fs.battleshipCount + fs.cruiserCount + fs.fighterCount + fs.vehicleCount > 1);
            transferButton.visible(secondary == null && fs.battleshipCount + fs.cruiserCount + fs.fighterCount + fs.vehicleCount > 0 && fleet().fleetsInRange(20).size() > 0);
            deleteButton.visible(secondary == null && f.inventory.size() == 0 && f.owner == player());
        }
    }

    /** 
	 * Clear the cells. 
	 * @param cells the list cells
	 */
    void clearCells(List<VehicleCell> cells) {
        for (VehicleCell vc : cells) {
            vc.type = null;
            vc.count = 0;
        }
    }

    /** 
	 * Initialize the cells according to the available fighter and vehicles.
	 * @param p the planet to use for the inventory count
	 * @param f the fleet to use for the inventory count 
	 * @param fighters the fighter cells to use
	 * @param tanks the tank cells
	 */
    void prepareCells(Planet p, Fleet f, List<VehicleCell> fighters, List<VehicleCell> tanks) {
        clearCells(fighters);
        clearCells(tanks);
        for (ResearchType rt : world().researches.values()) {
            if (!world().canDisplayResearch(rt)) {
                continue;
            }
            VehicleCell vc = null;
            if (rt.category == ResearchSubCategory.SPACESHIPS_FIGHTERS) {
                vc = fighters.get(rt.index);
            } else if (rt.category == ResearchSubCategory.WEAPONS_TANKS) {
                vc = tanks.get(rt.index);
            } else if (rt.category == ResearchSubCategory.WEAPONS_VEHICLES) {
                vc = tanks.get(rt.index + 4);
            }
            if (vc != null && player().isAvailable(rt)) {
                vc.type = rt;
                if (p != null) {
                    vc.count = p.inventoryCount(rt, player());
                } else if (f != null) {
                    vc.count = f.inventoryCount(rt);
                }
            }
        }
    }

    /** Go to previous planet/fleet. */
    void doPrev() {
        if (player().selectionMode == SelectionMode.PLANET) {
            List<Planet> planets = player().ownPlanets();
            Collections.sort(planets, Planet.NAME_ORDER);
            int idx = planets.indexOf(planet());
            if (idx > 0) {
                player().currentPlanet = planets.get(idx - 1);
            }
            updateInventory(planet(), null, leftList);
        } else {
            List<Fleet> fleets = player().ownFleets();
            int idx = fleets.indexOf(fleet());
            if (idx > 0) {
                player().currentFleet = fleets.get(idx - 1);
            }
            updateInventory(null, fleet(), leftList);
        }
        configure.type = null;
        configure.item = null;
        configure.selectedSlot = null;
    }

    /** Go to next planet/fleet. */
    void doNext() {
        if (player().selectionMode == SelectionMode.PLANET) {
            List<Planet> planets = player().ownPlanets();
            Collections.sort(planets, Planet.NAME_ORDER);
            int idx = planets.indexOf(planet());
            if (idx < planets.size() - 1 && planets.size() > 0) {
                player().currentPlanet = planets.get(idx + 1);
            }
            updateInventory(planet(), null, leftList);
            minimap.moveTo(planet().x, planet().y);
        } else {
            List<Fleet> fleets = player().ownFleets();
            int idx = fleets.indexOf(fleet());
            if (idx < fleets.size() - 1 && fleets.size() > 0) {
                player().currentFleet = fleets.get(idx + 1);
            }
            updateInventory(null, fleet(), leftList);
            minimap.moveTo(fleet().x, fleet().y);
        }
        configure.type = null;
        configure.item = null;
        configure.selectedSlot = null;
    }

    /** 
	 * Update inventory listing. 
	 * @param p the planet to use
	 * @param f the fleet to use
	 * @param list the target listing
	 */
    void updateInventory(Planet p, Fleet f, VehicleList list) {
        list.clear();
        if (p != null) {
            list.group = false;
            for (InventoryItem pii : p.inventory) {
                if (pii.type.category == ResearchSubCategory.SPACESHIPS_STATIONS) {
                    list.items.add(pii);
                }
            }
        } else {
            list.group = true;
            for (InventoryItem pii : f.inventory) {
                if (pii.type.category == ResearchSubCategory.SPACESHIPS_BATTLESHIPS || pii.type.category == ResearchSubCategory.SPACESHIPS_CRUISERS) {
                    list.items.add(pii);
                }
            }
        }
        list.compute();
    }

    /**
	 * Select the given vehicle.
	 * @param rt the research type
	 */
    void doSelectVehicle(ResearchType rt) {
        for (VehicleCell vc : leftFighterCells) {
            vc.selected = vc.type == rt && rt != null;
        }
        for (VehicleCell vc : rightFighterCells) {
            vc.selected = vc.type == rt && rt != null;
        }
        for (VehicleCell vc : leftTankCells) {
            vc.selected = vc.type == rt && rt != null;
        }
        for (VehicleCell vc : rightTankCells) {
            vc.selected = vc.type == rt && rt != null;
        }
    }

    /**
	 * Display the elements of the given sub-category in the slots.
	 * @param cat the target category
	 */
    void displayCategory(ResearchSubCategory cat) {
        for (TechnologySlot ts : slots) {
            ts.type = null;
            ts.visible(false);
        }
        for (ResearchType rt : world().researches.values()) {
            if (world().canDisplayResearch(rt) && rt.category == cat) {
                updateSlot(rt);
            }
        }
        doSelectCategoryButtons(cat);
    }

    /**
	 * Select the category buttons based on the category.
	 * @param cat the category
	 */
    void doSelectCategoryButtons(ResearchSubCategory cat) {
        battleships.down = cat == ResearchSubCategory.SPACESHIPS_BATTLESHIPS;
        cruisers.down = cat == ResearchSubCategory.SPACESHIPS_CRUISERS;
        fighters.down = cat == ResearchSubCategory.SPACESHIPS_FIGHTERS;
        tanks.down = cat == ResearchSubCategory.WEAPONS_TANKS;
        vehicles.down = cat == ResearchSubCategory.WEAPONS_VEHICLES;
        stations.down = cat == ResearchSubCategory.SPACESHIPS_STATIONS;
    }

    /** Add a ship or vehicle to the planet or fleet. */
    void doAddItem() {
        addRemoveItem(1);
    }

    /** Remove a fighter or vehicle from the planet or fleet. */
    void doRemoveItem() {
        addRemoveItem(-1);
    }

    /**
	 * Add or remove a given amount of the currently selected research.
	 * @param delta the delta
	 */
    void addRemoveItem(int delta) {
        if (player().selectionMode == SelectionMode.PLANET) {
            if (research().category == ResearchSubCategory.SPACESHIPS_STATIONS) {
                if (delta > 0) {
                    InventoryItem pii = new InventoryItem();
                    pii.owner = player();
                    pii.type = research();
                    pii.count = 1;
                    pii.hp = pii.type.productionCost;
                    pii.shield = pii.type.productionCost;
                    planet().inventory.add(pii);
                    leftList.items.add(pii);
                    leftList.compute();
                }
            } else {
                planet().changeInventory(research(), player(), delta);
            }
        } else {
            if (research().category == ResearchSubCategory.SPACESHIPS_CRUISERS || research().category == ResearchSubCategory.SPACESHIPS_BATTLESHIPS) {
                if (delta > 0) {
                    int cnt = fleet().inventoryCount(research());
                    List<InventoryItem> iss = fleet().addInventory(research(), delta);
                    leftList.items.clear();
                    leftList.items.addAll(fleet().getSingleItems());
                    leftList.compute();
                    configure.item = iss.get(0);
                    leftList.map.get(research()).index = cnt;
                }
                if (config.computerVoiceScreen) {
                    commons.sounds.play(SoundType.SHIP_DEPLOYED);
                }
            } else {
                fleet().changeInventory(research(), delta);
            }
        }
        player().changeInventoryCount(research(), -delta);
        doSelectListVehicle(research());
    }

    /** Update the inventory display based on the current selection. */
    void updateCurrentInventory() {
        if (player().selectionMode == SelectionMode.PLANET) {
            updateInventory(planet(), null, leftList);
        } else {
            updateInventory(null, fleet(), leftList);
        }
        doSelectListVehicle(research());
    }

    /** 
	 * Update the list selection.
	 * @param value the new selected type
	 */
    void doSelectListVehicle(ResearchType value) {
        if ((rightList.selectedItem != null && rightList.selectedItem.type != value) || (rightList.items.size() == 0)) {
            rightList.selectedItem = null;
        } else {
            for (InventoryItem pii : rightList.items) {
                if (pii.type == value) {
                    if (rightList.group) {
                        InventoryItemGroup ig = rightList.map.get(value);
                        rightList.selectedItem = ig.items.get(ig.index);
                    } else {
                        rightList.selectedItem = pii;
                    }
                    break;
                }
            }
        }
        if ((leftList.selectedItem != null && leftList.selectedItem.type != value) || (leftList.items.size() == 0)) {
            leftList.selectedItem = null;
        } else {
            for (InventoryItem pii : leftList.items) {
                if (pii.type == value) {
                    if (leftList.group) {
                        InventoryItemGroup ig = leftList.map.get(value);
                        leftList.selectedItem = ig.items.get(ig.index);
                    } else {
                        leftList.selectedItem = pii;
                    }
                    break;
                }
            }
        }
    }

    /** 
	 * Select the items suitable for the given inventory slot. 
	 * @param slot the target inventory slot
	 */
    void onSelectInventorySlot(InventorySlot slot) {
        if (slot != null) {
            for (TechnologySlot ts : slots) {
                ts.visible(false);
                ts.type = null;
            }
            for (ResearchType rt : slot.slot.items) {
                updateSlot(rt);
            }
            if (slot.type != null) {
                world().selectResearch(slot.type);
            } else {
                world().selectResearch(slot.slot.items.get(0));
            }
            configure.selectedSlot = slot;
            doSelectCategoryButtons(research().category);
        } else {
            configure.selectedSlot = null;
            world().selectResearch(configure.item.type);
            displayCategory(research().category);
        }
    }

    /**
	 * Select an inventory item from the inventory listing.
	 * @param value select the given inventory item
	 */
    void onSelectInventoryItem(InventoryItem value) {
        if (value.type != research() && (configure.selectedSlot == null || configure.item == null || configure.item.type != value.type)) {
            displayCategory(value.type.category);
            research(value.type);
        }
        leftList.selectedItem = value;
        rightList.selectedItem = value;
        configure.type = value.type;
        configure.item = value;
        if (configure.selectedSlot != null) {
            configure.selectedSlot = configure.item.getSlot(configure.selectedSlot.slot.id);
        }
        doSelectVehicle(value.type);
    }

    /**
	 * Select the given vehicle slot (which is not in the listing).
	 * @param value the vehicle slot
	 */
    void onSelectVehicleSlot(ResearchType value) {
        world().selectResearch(value);
        displayCategory(value.category);
        leftList.selectedItem = null;
        rightList.selectedItem = null;
        configure.type = value;
        configure.item = null;
        configure.selectedSlot = null;
        doSelectVehicle(value);
    }

    /**
	 * Select the given research slot.
	 * @param value the value
	 */
    void onSelectResearchSlot(ResearchType value) {
        world().selectResearch(value);
        if (configure.selectedSlot == null) {
            doSelectVehicle(value);
            doSelectListVehicle(value);
            configure.type = value;
            configure.item = leftList.selectedItem;
        }
    }

    /** Add one or replace the current slot contents. */
    void doAddOne() {
        if (configure.selectedSlot.type != research()) {
            configure.selectedSlot.type = research();
            configure.selectedSlot.count = 1;
            configure.selectedSlot.hp = research().productionCost;
            if (research().has("shield")) {
                configure.item.shield = configure.item.shieldMax();
            }
        } else {
            configure.selectedSlot.count++;
        }
        player().changeInventoryCount(configure.selectedSlot.type, -1);
    }

    /** Remove one item from the current slot. */
    void doRemoveOne() {
        player().changeInventoryCount(configure.selectedSlot.type, 1);
        configure.selectedSlot.count--;
        if (configure.selectedSlot.type.has("shield")) {
            configure.item.shield = 0;
        }
        if (configure.selectedSlot.count == 0) {
            configure.selectedSlot.type = null;
        }
    }

    @Override
    public boolean keyboard(KeyEvent e) {
        if (editPrimary || editSecondary) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                editPrimary = false;
                editSecondary = false;
                e.consume();
            }
            if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                if (editPrimary) {
                    if (fleet().name.length() > 0) {
                        fleet().name = fleet().name.substring(0, fleet().name.length() - 1);
                    }
                }
                if (editSecondary) {
                    if (secondary.name.length() > 0) {
                        secondary.name = secondary.name.substring(0, secondary.name.length() - 1);
                    }
                }
                e.consume();
            } else if (commons.text().isSupported(e.getKeyChar())) {
                if (editPrimary) {
                    fleet().name += e.getKeyChar();
                }
                if (editSecondary) {
                    secondary.name += e.getKeyChar();
                }
                e.consume();
            }
        }
        return super.keyboard(e);
    }

    /**
	 * Split the current fleet to another.
	 */
    void doSplit() {
        secondary = doCreateFleet(false, fleet().x, fleet().y);
        clearCells(rightFighterCells);
        clearCells(rightTankCells);
        rightList.clear();
        fleetListing.nearby = false;
        editSecondary = true;
    }

    /** End split. */
    void doEndSplit() {
        if (fleet().inventory.size() == 0) {
            player().fleets.remove(fleet());
            player().currentFleet = secondary;
        }
        if (secondary.inventory.size() == 0) {
            player().fleets.remove(secondary);
        }
        if (secondary.inventory.contains(configure.item)) {
            configure.item = fleet().getInventoryItem(configure.item.type);
        }
        secondary = null;
        editSecondary = false;
        onSelectResearchSlot(research());
    }

    /**
	 * Move the given amount of the inventory item into the destination fleet.
	 * @param src the source fleet
	 * @param dst the destination fleet
	 * @param type the item type to move
	 * @param mode the transfer mode: 1 - one, 2 - half, 3 - full
	 * @param startIndex the start index for the given type (when grouped items are transferred)
	 */
    void doMoveItem(Fleet src, Fleet dst, ResearchType type, int mode, int startIndex) {
        int srcCount = src.inventoryCount(type);
        if (srcCount > 0) {
            int transferCount = 1;
            if (mode == 2) {
                transferCount = (srcCount + 1) / 2;
            } else if (mode == 3) {
                transferCount = srcCount;
            }
            transferCount = Math.min(transferCount, dst.getAddLimit(type));
            if (type.category == ResearchSubCategory.SPACESHIPS_CRUISERS || type.category == ResearchSubCategory.SPACESHIPS_BATTLESHIPS) {
                for (Iterator<InventoryItem> it = src.inventory.iterator(); it.hasNext(); ) {
                    InventoryItem ii = it.next();
                    if (ii.type == type) {
                        if (startIndex == 0) {
                            it.remove();
                            dst.inventory.add(ii);
                            transferCount--;
                        } else {
                            startIndex--;
                        }
                    }
                    if (transferCount == 0) {
                        break;
                    }
                }
                FleetStatistics fs = src.getStatistics();
                if (fs.vehicleCount > fs.vehicleMax) {
                    int delta = fs.vehicleCount - fs.vehicleMax;
                    for (int j = src.inventory.size() - 1; j >= 0; j--) {
                        InventoryItem ii = src.inventory.get(j);
                        if (ii.type.category == ResearchSubCategory.WEAPONS_TANKS || ii.type.category == ResearchSubCategory.WEAPONS_VEHICLES) {
                            int toremove = delta > ii.count ? ii.count : delta;
                            dst.changeInventory(ii.type, toremove);
                            src.changeInventory(ii.type, -toremove);
                            delta -= toremove;
                            if (delta == 0) {
                                break;
                            }
                        }
                    }
                }
                updateInventory(null, fleet(), leftList);
                updateInventory(null, secondary, rightList);
            } else {
                src.changeInventory(type, -transferCount);
                dst.changeInventory(type, transferCount);
            }
        }
    }

    /** End join. */
    void doEndJoin() {
        transferMode = false;
        fleetListing.nearby = false;
        fleetListing.selected = null;
        doEndSplit();
    }

    /** Initiate the transfer mode. */
    void doTransfer() {
        transferMode = true;
        fleetListing.nearby = true;
        fleetListing.visible(true);
    }

    /** Delete an empty fleet. */
    void doDeleteFleet() {
        Fleet f = fleet();
        if (f != null && f.owner == player() && f.inventory.size() == 0) {
            List<Fleet> fs = player().ownFleets();
            int idx = fs.indexOf(f);
            player().fleets.remove(fleet());
            if (idx >= 0 && player().fleets.size() > 0) {
                if (idx < fs.size()) {
                    player().currentFleet = fs.get(idx > 0 ? idx - 1 : 1);
                    minimap.moveTo(fleet().x, fleet().y);
                }
            } else {
                player().currentFleet = null;
                player().selectionMode = SelectionMode.PLANET;
            }
        }
    }

    /** Sell one of the currently selected ship or vehicle. */
    void doSell() {
        switch(research().category) {
            case SPACESHIPS_BATTLESHIPS:
            case SPACESHIPS_CRUISERS:
            case SPACESHIPS_FIGHTERS:
            case SPACESHIPS_STATIONS:
            case WEAPONS_TANKS:
            case WEAPONS_VEHICLES:
                InventoryItem ii = null;
                if (leftList.selectedItem != null) {
                    ii = leftList.selectedItem;
                } else if (player().selectionMode == SelectionMode.FLEET) {
                    ii = fleet().getInventoryItem(research());
                } else {
                    ii = planet().getInventoryItem(research(), player());
                }
                if (ii != null) {
                    int worth = ii.type.productionCost;
                    for (InventorySlot is : ii.slots) {
                        if (is.type != null) {
                            worth += is.type.productionCost;
                        }
                    }
                    if (player().selectionMode == SelectionMode.FLEET) {
                        if (leftList.selectedItem != null) {
                            InventoryItemGroup ig = leftList.map.get(research());
                            ig.items.remove(ig.index);
                            ig.index = Math.min(ig.index, ig.items.size() - 1);
                            if (ig.index >= 0) {
                                leftList.selectedItem = ig.items.get(ig.index);
                                configure.item = leftList.selectedItem;
                            } else {
                                configure.item = null;
                            }
                            fleet().inventory.remove(ii);
                        } else {
                            fleet().changeInventory(research(), -1);
                        }
                        updateInventory(null, fleet(), leftList);
                    } else {
                        if (leftList.selectedItem != null) {
                            planet().inventory.remove(ii);
                        } else {
                            planet().changeInventory(research(), player(), -1);
                        }
                        updateInventory(planet(), null, leftList);
                    }
                    worth /= 2;
                    player().money += worth;
                    player().statistics.moneyIncome += worth;
                    player().statistics.moneySellIncome += worth;
                    world().statistics.moneyIncome += worth;
                    world().statistics.moneySellIncome += worth;
                }
                break;
            default:
        }
    }
}
