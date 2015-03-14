/*
 * This file is part of Bitsquare.
 *
 * Bitsquare is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * Bitsquare is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Bitsquare. If not, see <http://www.gnu.org/licenses/>.
 */

package io.bitsquare.trade.protocol.trade.offerer.tasks;

import io.bitsquare.btc.FeePolicy;
import io.bitsquare.trade.protocol.trade.offerer.BuyerAsOffererModel;
import io.bitsquare.util.taskrunner.Task;
import io.bitsquare.util.taskrunner.TaskRunner;

import org.bitcoinj.core.Coin;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.Transaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrepareDepositTx extends Task<BuyerAsOffererModel> {
    private static final Logger log = LoggerFactory.getLogger(PrepareDepositTx.class);

    public PrepareDepositTx(TaskRunner taskHandler, BuyerAsOffererModel model) {
        super(taskHandler, model);
    }

    @Override
    protected void doRun() {
        try {
            byte[] offererPubKey = model.getWalletService().getAddressInfo(model.getTrade().getId()).getPubKey();
            Coin offererInputAmount = model.getTrade().getSecurityDeposit().add(FeePolicy.TX_FEE);
            Transaction transaction = model.getWalletService().offererPreparesDepositTx(
                    offererInputAmount,
                    model.getTrade().getId(),
                    offererPubKey,
                    model.getTakerPubKey(),
                    model.getArbitratorPubKey());

            long offererTxOutIndex = transaction.getInput(0).getOutpoint().getIndex();

            model.setOffererPubKey(offererPubKey);
            model.setPreparedDepositTx(transaction);
            model.setOffererTxOutIndex(offererTxOutIndex);

            complete();
        } catch (InsufficientMoneyException e) {
            failed(e);
        }
    }

    @Override
    protected void updateStateOnFault() {
    }
}