package agents;

import behaviours.ArchiveStockData;
import behaviours.PriceInform;
import gui.HistorianGui;
import jade.core.Agent;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import models.Asset;
import models.MarketOfAssets;
import utils.DfAgentUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Przemek on 2017-06-20.
 */
public class Historian extends Agent {
    private DfAgentUtils utils;
    private List<Asset> assets = new ArrayList<Asset>();
    private MessageTemplate priceInformTemplate;
    private MessageTemplate archiveStockDataTemplate;
    private HistorianGui gui;

    @Override
    protected void setup() {
        System.out.println("Historian agent " + getAID().getName() + " is ready");

        utils = new DfAgentUtils(this);
        utils.registerService("historian", "historianAgent");

        setStartupPrices();

        setPriceCheckTemplateAttributes();
        addBehaviour(new PriceInform(this, priceInformTemplate));
        setArchiveStockDataTemplateAttributes();
        addBehaviour(new ArchiveStockData(this, archiveStockDataTemplate));
        gui = new HistorianGui(assets);
        gui.showGui();
    }

    @Override
    protected void takeDown() {
        System.out.println("Historian agent going down");
        utils.deregisterService();
    }

    public BigDecimal getAssetCurrentPrice(Asset assetToCheckPrice) {
        return (findAsset(assetToCheckPrice).getUnitValue());
    }

    public void archiveData(List<Asset> newPricesToArchive) {
        assets.clear();
        assets.addAll(newPricesToArchive);
        gui.addNewPrices(newPricesToArchive);
    }

    private void setStartupPrices() {
        MarketOfAssets market = new MarketOfAssets();
        assets.addAll(market.getAssetsOnMarket());
    }

    private Asset findAsset(Asset assetToFind) {
        Asset foundAsset = null;
        for (Asset a : assets) {
            if (a.equals(assetToFind)) {
                foundAsset = a;
                break;
            }
        }
        return foundAsset;
    }

    private void setPriceCheckTemplateAttributes() {
        priceInformTemplate = MessageTemplate.and(
                MessageTemplate.and(
                    MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST),
                    MessageTemplate.MatchPerformative(ACLMessage.REQUEST)),
                MessageTemplate.MatchConversationId("price-check"));
    }

    private void setArchiveStockDataTemplateAttributes() {
        archiveStockDataTemplate = MessageTemplate.and(
                MessageTemplate.and(
                        MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST),
                        MessageTemplate.MatchPerformative(ACLMessage.REQUEST)),
                MessageTemplate.MatchConversationId("archive-prices"));
    }
}
