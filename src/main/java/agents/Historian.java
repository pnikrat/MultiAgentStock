package agents;

import behaviours.PriceInformBehaviour;
import jade.core.AID;
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
    private MessageTemplate priceCheckTemplate;

    @Override
    protected void setup() {
        System.out.println("Historian agent " + getAID().getName() + " is ready");

        utils = new DfAgentUtils(this);
        utils.registerService("historian", "historianAgent");

        setStartupPrices();

        setPriceCheckTemplateAttributes();
        addBehaviour(new PriceInformBehaviour(this, priceCheckTemplate));
    }

    @Override
    protected void takeDown() {
        System.out.println("Historian agent going down");
        utils.deregisterService();
    }

    public BigDecimal getAssetCurrentPrice(Asset assetToCheckPrice) {
        return (findAsset(assetToCheckPrice).getUnitValue());
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
        priceCheckTemplate = MessageTemplate.and(
                MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST),
                MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
    }
}
