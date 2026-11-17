package com.example.offsuitpoker.network

object WebClientHtml {
    fun getHtml(): String {
        return """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no, viewport-fit=cover">
<title>Runner Runner Nuts - Poker</title>
<style>
  * { box-sizing: border-box; margin: 0; padding: 0; -webkit-tap-highlight-color: transparent; }
  body {
    background-color: #08080A;
    color: #EDEDED;
    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
    min-height: 100vh;
    display: flex;
    flex-direction: column;
    overflow-x: hidden;
    user-select: none;
    -webkit-user-select: none;
  }
  header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 16px;
    background: #0D0D12;
    border-bottom: 1px solid #1E1E28;
  }
  .app-title { font-size: 13px; font-weight: 900; letter-spacing: 1.5px; color: #D4AF37; }
  .table-badge { font-size: 11px; font-family: monospace; color: #8E8EA0; background: #151520; padding: 4px 8px; border-radius: 6px; }

  /* Poker Table Felt Area */
  .table-container {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    padding: 12px 8px;
    position: relative;
  }
  .opponents-row {
    display: flex;
    justify-content: space-around;
    align-items: flex-start;
    flex-wrap: wrap;
    gap: 8px;
    min-height: 90px;
  }
  .player-pod {
    background: #111118;
    border: 1px solid #242432;
    border-radius: 10px;
    padding: 6px 10px;
    min-width: 85px;
    text-align: center;
    position: relative;
    transition: all 0.25s ease;
  }
  .player-pod.active-turn {
    border-color: #D4AF37;
    box-shadow: 0 0 10px rgba(212, 175, 55, 0.4);
    transform: scale(1.05);
  }
  .player-pod.folded { opacity: 0.45; filter: grayscale(80%); }
  .pod-name { font-size: 11px; font-weight: 700; color: #FFF; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 90px; }
  .pod-chips { font-size: 11px; font-weight: 800; color: #22C55E; font-family: monospace; margin-top: 2px; }
  .pod-bet { font-size: 10px; color: #D4AF37; font-family: monospace; margin-top: 2px; }
  .dealer-btn {
    position: absolute;
    top: -6px;
    right: -6px;
    background: #D4AF37;
    color: #08080A;
    font-size: 9px;
    font-weight: 900;
    width: 16px;
    height: 16px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    border: 1px solid #FFF;
  }

  /* Center Felt: Pot & Community Cards */
  .center-felt {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    margin: 16px 0;
  }
  .pot-capsule {
    background: #14141E;
    border: 1px solid #D4AF37;
    border-radius: 20px;
    padding: 6px 16px;
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 12px;
  }
  .pot-label { font-size: 10px; font-weight: 800; color: #9E9EB0; letter-spacing: 1px; }
  .pot-value { font-size: 15px; font-weight: 900; color: #D4AF37; font-family: monospace; }

  .community-cards {
    display: flex;
    gap: 6px;
    min-height: 64px;
    align-items: center;
  }

  /* Cards Styling (4-Color Deck) */
  .card {
    width: 44px;
    height: 62px;
    background: #FFF;
    border-radius: 6px;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    padding: 3px 4px;
    box-shadow: 0 2px 6px rgba(0,0,0,0.6);
    position: relative;
    font-family: -apple-system, monospace;
    font-weight: 900;
  }
  .card.large { width: 56px; height: 80px; padding: 6px; }
  .card-top { font-size: 12px; line-height: 1; display: flex; flex-direction: column; align-items: flex-start; }
  .card.large .card-top { font-size: 15px; }
  .card-suit-big { font-size: 18px; align-self: center; line-height: 1; }
  .card.large .card-suit-big { font-size: 26px; }
  .card-back {
    background: linear-gradient(135deg, #1C1C28 0%, #0A0A10 100%);
    border: 1px solid #2E2E40;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #D4AF37;
    font-size: 14px;
  }
  /* 4-Color deck colors: Hearts=Red, Diamonds=Blue, Clubs=Green, Spades=Black */
  .suit-h { color: #E11D48; }
  .suit-d { color: #2563EB; }
  .suit-c { color: #16A34A; }
  .suit-s { color: #18181B; }

  /* Winner Announcement Banner */
  .winner-banner {
    background: #14141E;
    border: 1px solid #D4AF37;
    border-radius: 8px;
    padding: 8px 14px;
    margin: 8px auto;
    font-size: 11.5px;
    font-weight: 800;
    color: #EDEDED;
    text-align: center;
    font-family: monospace;
    max-width: 95%;
  }

  /* Bottom User Section */
  .local-player-section {
    background: #0E0E14;
    border: 1px solid #1E1E2A;
    border-radius: 16px 16px 0 0;
    padding: 12px 14px;
    display: flex;
    flex-direction: column;
    gap: 10px;
    padding-bottom: max(12px, env(safe-area-inset-bottom));
  }
  .local-meta {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  .local-name-stars { display: flex; align-items: center; gap: 8px; }
  .local-name { font-size: 13px; font-weight: 800; color: #FFF; }
  .local-stars { font-size: 12px; color: #D4AF37; letter-spacing: 1px; }
  .local-bankroll { font-size: 14px; font-weight: 900; color: #22C55E; font-family: monospace; }

  .local-cards-row {
    display: flex;
    justify-content: center;
    align-items: center;
    gap: 12px;
    margin: 4px 0;
  }

  /* Actions Bar */
  .actions-bar {
    display: flex;
    gap: 8px;
    width: 100%;
  }
  .action-btn {
    flex: 1;
    padding: 12px 6px;
    border: none;
    border-radius: 10px;
    font-weight: 900;
    font-size: 13px;
    font-family: monospace;
    cursor: pointer;
    text-transform: uppercase;
    transition: all 0.15s ease;
  }
  .action-btn:active { transform: scale(0.97); }
  .action-btn:disabled { opacity: 0.35; cursor: not-allowed; }
  .btn-fold { background: #1C1215; color: #EF4444; border: 1px solid #7F1D1D; }
  .btn-check-call { background: #122118; color: #22C55E; border: 1px solid #15803D; }
  .btn-bet-raise { background: #D4AF37; color: #08080A; border: 1px solid #E6CA65; }
  .btn-allin { background: #261510; color: #F97316; border: 1px solid #C2410C; }

  /* Raise Slider Controls */
  .raise-controls {
    display: flex;
    flex-direction: column;
    gap: 8px;
    background: #14141E;
    padding: 10px 12px;
    border-radius: 10px;
    border: 1px solid #2B2B3C;
  }
  .slider-row { display: flex; align-items: center; gap: 10px; }
  input[type=range] {
    flex: 1;
    accent-color: #D4AF37;
    height: 6px;
  }
  .quick-chips {
    display: flex;
    justify-content: space-between;
    gap: 4px;
  }
  .chip-pill {
    flex: 1;
    background: #1E1E2C;
    border: 1px solid #333346;
    color: #EDEDED;
    padding: 5px 2px;
    font-size: 10px;
    font-weight: 700;
    border-radius: 6px;
    text-align: center;
    cursor: pointer;
    font-family: monospace;
  }

  /* Join Modal Overlay */
  .modal-overlay {
    position: fixed;
    top: 0; left: 0; right: 0; bottom: 0;
    background: rgba(0, 0, 0, 0.88);
    backdrop-filter: blur(8px);
    -webkit-backdrop-filter: blur(8px);
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 20px;
    z-index: 1000;
  }
  .modal-card {
    background: #101016;
    border: 1px solid #D4AF37;
    border-radius: 16px;
    padding: 24px 20px;
    width: 100%;
    max-width: 340px;
    text-align: center;
  }
  .modal-title { font-size: 16px; font-weight: 900; color: #D4AF37; margin-bottom: 8px; letter-spacing: 1px; }
  .modal-desc { font-size: 12px; color: #9E9EB0; margin-bottom: 18px; line-height: 1.4; }
  .input-field {
    width: 100%;
    padding: 12px 14px;
    background: #181822;
    border: 1px solid #2E2E40;
    border-radius: 10px;
    color: #FFF;
    font-size: 14px;
    font-weight: bold;
    margin-bottom: 18px;
    outline: none;
    text-align: center;
  }
  .input-field:focus { border-color: #D4AF37; }
  .btn-join {
    width: 100%;
    padding: 14px;
    background: #D4AF37;
    color: #0C0C0E;
    border: none;
    border-radius: 10px;
    font-size: 13.5px;
    font-weight: 900;
    font-family: monospace;
    cursor: pointer;
  }
</style>
</head>
<body>

<header>
  <div class="app-title">RUNNER RUNNER NUTS</div>
  <div class="table-badge" id="handBadge">HAND #1</div>
</header>

<div class="table-container">
  <!-- Top Opponents Area -->
  <div class="opponents-row" id="opponentsRow">
    <div style="font-size: 11px; color: #555; margin-top: 15px;">Waiting for table state...</div>
  </div>

  <!-- Center Felt: Community Cards & Pot -->
  <div class="center-felt">
    <div class="pot-capsule">
      <span class="pot-label">TOTAL POT</span>
      <span class="pot-value" id="potDisplay">₹0</span>
    </div>

    <div class="community-cards" id="communityCardsRow">
      <!-- 5 community cards -->
    </div>

    <div id="winnerBannerArea" style="width: 100%;"></div>
  </div>

  <!-- Bottom: Local Player Area -->
  <div class="local-player-section">
    <div class="local-meta">
      <div class="local-name-stars">
        <span class="local-name" id="myPlayerName">Player</span>
        <span class="local-stars" id="myPlayerStars">★★★★★</span>
      </div>
      <div class="local-bankroll" id="myChipsDisplay">₹10,000</div>
    </div>

    <div class="local-cards-row" id="myCardsRow">
      <!-- 2 Hole Cards -->
    </div>

    <!-- Raise / Bet Slider Box -->
    <div class="raise-controls" id="raiseControlsBox" style="display: none;">
      <div class="slider-row">
        <input type="range" id="raiseSlider" min="100" max="10000" step="50" value="200">
        <span id="sliderValueText" style="font-family: monospace; font-size: 13px; font-weight: 800; color: #D4AF37; min-width: 65px;">₹200</span>
      </div>
      <div class="quick-chips">
        <div class="chip-pill" onclick="setQuickBet(0.5)">½ POT</div>
        <div class="chip-pill" onclick="setQuickBet(1.0)">POT</div>
        <div class="chip-pill" onclick="setQuickBet(2.0)">2X</div>
        <div class="chip-pill" onclick="setQuickBet('ALL')">ALL IN</div>
      </div>
    </div>

    <!-- Action Buttons Row -->
    <div class="actions-bar" id="actionsBar">
      <button class="action-btn btn-fold" id="btnFold" onclick="sendAction('FOLD')">FOLD</button>
      <button class="action-btn btn-check-call" id="btnCheckCall" onclick="sendCheckOrCall()">CHECK</button>
      <button class="action-btn btn-bet-raise" id="btnRaise" onclick="toggleOrSendRaise()">BET</button>
      <button class="action-btn btn-allin" id="btnAllIn" onclick="sendAction('ALL_IN')">ALL IN</button>
    </div>
  </div>
</div>

<!-- Join Modal Dialog -->
<div class="modal-overlay" id="joinModal">
  <div class="modal-card">
    <div class="modal-title">RUNNER RUNNER NUTS</div>
    <div class="modal-desc">Enter your player name to join the live Texas Hold'em table directly from Safari!</div>
    <input type="text" class="input-field" id="nameInput" placeholder="Your Name" maxlength="12" value="iPhone Player">
    <button class="btn-join" onclick="joinTable()">JOIN TABLE</button>
  </div>
</div>

<script>
  let socket = null;
  let myId = localStorage.getItem('rrn_player_id') || ('P-' + Math.random().toString(36).substring(2, 8).toUpperCase());
  localStorage.setItem('rrn_player_id', myId);

  let currentTable = null;
  let mySeat = null;
  let isRaiseSliderOpen = false;

  // Sound Synthesizer via Web Audio API (Zero downloads needed)
  const audioCtx = new (window.AudioContext || window.webkitAudioContext)();
  function playBeep(freq, type, duration) {
    try {
      if (audioCtx.state === 'suspended') audioCtx.resume();
      const osc = audioCtx.createOscillator();
      const gain = audioCtx.createGain();
      osc.type = type || 'sine';
      osc.frequency.value = freq;
      gain.gain.setValueAtTime(0.12, audioCtx.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.001, audioCtx.currentTime + duration);
      osc.connect(gain);
      gain.connect(audioCtx.destination);
      osc.start();
      osc.stop(audioCtx.currentTime + duration);
    } catch(e) {}
  }
  function playChipSound() { playBeep(800, 'triangle', 0.08); }
  function playTurnAlert() { playBeep(580, 'sine', 0.15); setTimeout(() => playBeep(880, 'sine', 0.2), 150); }
  function playWinSound() {
    [523, 659, 783, 1046].forEach((f, idx) => setTimeout(() => playBeep(f, 'triangle', 0.25), idx * 120));
  }

  function initSocket() {
    const wsUrl = 'ws://' + location.hostname + ':8088';
    socket = new WebSocket(wsUrl);

    socket.onopen = () => {
      console.log('Connected to Poker WebSocket');
      const savedName = localStorage.getItem('rrn_name');
      if (savedName) {
        document.getElementById('nameInput').value = savedName;
        joinTable();
      }
    };

    socket.onmessage = (event) => {
      try {
        const msg = JSON.parse(event.data);
        if (msg.type === 'STATE' && msg.stateJson) {
          const state = JSON.parse(msg.stateJson);
          renderTable(state);
        }
      } catch (err) {
        console.error('Error handling WS message:', err);
      }
    };

    socket.onclose = () => {
      console.log('Socket closed, reconnecting in 2s...');
      setTimeout(initSocket, 2000);
    };
  }

  function joinTable() {
    const name = document.getElementById('nameInput').value.trim() || 'Player';
    localStorage.setItem('rrn_name', name);
    document.getElementById('joinModal').style.display = 'none';

    if (socket && socket.readyState === WebSocket.OPEN) {
      socket.send(JSON.stringify({
        type: 'JOIN',
        playerId: myId,
        playerName: name,
        stars: 5,
        chips: 10000
      }));
    }
  }

  function sendAction(actionType, amount) {
    playChipSound();
    if (socket && socket.readyState === WebSocket.OPEN) {
      socket.send(JSON.stringify({
        type: 'ACTION',
        playerId: myId,
        actionType: actionType,
        amount: amount || 0
      }));
    }
    isRaiseSliderOpen = false;
    document.getElementById('raiseControlsBox').style.display = 'none';
  }

  function sendCheckOrCall() {
    if (!currentTable || !mySeat) return;
    const callAmount = (currentTable.currentHighBet || 0) - (mySeat.roundBet || 0);
    if (callAmount > 0) {
      sendAction('CALL', callAmount);
    } else {
      sendAction('CHECK', 0);
    }
  }

  function toggleOrSendRaise() {
    if (!isRaiseSliderOpen) {
      isRaiseSliderOpen = true;
      document.getElementById('raiseControlsBox').style.display = 'flex';
      updateSliderBounds();
    } else {
      const raiseAmt = parseInt(document.getElementById('raiseSlider').value, 10);
      sendAction('RAISE', raiseAmt);
    }
  }

  function setQuickBet(multiplier) {
    if (!currentTable || !mySeat) return;
    const slider = document.getElementById('raiseSlider');
    const pot = currentTable.mainPot || 100;
    if (multiplier === 'ALL') {
      slider.value = slider.max;
    } else {
      slider.value = Math.min(slider.max, Math.max(slider.min, Math.round(pot * multiplier)));
    }
    document.getElementById('sliderValueText').innerText = '₹' + slider.value;
  }

  document.getElementById('raiseSlider').addEventListener('input', (e) => {
    document.getElementById('sliderValueText').innerText = '₹' + e.target.value;
  });

  function updateSliderBounds() {
    if (!currentTable || !mySeat) return;
    const minRaise = (currentTable.currentHighBet || 0) + (currentTable.minRaiseAmount || currentTable.bigBlind || 100);
    const maxRaise = mySeat.chips + (mySeat.roundBet || 0);
    const slider = document.getElementById('raiseSlider');
    slider.min = Math.min(minRaise, maxRaise);
    slider.max = maxRaise;
    slider.value = slider.min;
    document.getElementById('sliderValueText').innerText = '₹' + slider.value;
  }

  function renderCard(card, isLarge) {
    if (!card) {
      return '<div class="card card-back' + (isLarge ? ' large' : '') + '">♠</div>';
    }
    const suitClass = 'suit-' + (card.suit ? card.suit.toLowerCase()[0] : 's');
    const suitSymbols = { 'HEARTS': '♥', 'DIAMONDS': '♦', 'CLUBS': '♣', 'SPADES': '♠' };
    const suitIcon = suitSymbols[card.suit] || '♠';
    const rankDisplay = card.rank?.display || card.rank || '?';

    return '<div class="card ' + suitClass + (isLarge ? ' large' : '') + '">' +
           '  <div class="card-top">' + rankDisplay + '</div>' +
           '  <div class="card-suit-big">' + suitIcon + '</div>' +
           '</div>';
  }

  function renderTable(table) {
    currentTable = table;
    document.getElementById('handBadge').innerText = 'HAND #' + (table.handCount || 1);
    document.getElementById('potDisplay').innerText = '₹' + (table.mainPot || 0).toLocaleString();

    // Community Cards
    const commRow = document.getElementById('communityCardsRow');
    let commHtml = '';
    const cards = table.communityCards || [];
    for (let i = 0; i < 5; i++) {
      commHtml += renderCard(cards[i] || null, false);
    }
    commRow.innerHTML = commHtml;

    // Find my seat and opponents
    const seats = table.seats || [];
    mySeat = seats.find(s => s.id === myId);

    // Render Opponents
    const oppRow = document.getElementById('opponentsRow');
    const opponents = seats.filter(s => s.id !== myId);
    let oppHtml = '';
    opponents.forEach((op, idx) => {
      const isActiveTurn = (table.activeTurnIndex !== -1 && seats[table.activeTurnIndex]?.id === op.id);
      const isDealer = (table.dealerIndex === seats.indexOf(op));
      const foldedClass = op.folded ? ' folded' : '';
      const turnClass = isActiveTurn ? ' active-turn' : '';

      oppHtml += '<div class="player-pod' + turnClass + foldedClass + '">' +
                 (isDealer ? '<div class="dealer-btn">D</div>' : '') +
                 '  <div class="pod-name">' + op.name + '</div>' +
                 '  <div class="pod-chips">₹' + (op.chips || 0).toLocaleString() + '</div>' +
                 (op.roundBet > 0 ? '<div class="pod-bet">Bet: ₹' + op.roundBet + '</div>' : '') +
                 '</div>';
    });
    oppRow.innerHTML = oppHtml || '<div style="font-size: 11px; color: #555;">No other players yet</div>';

    // Render Local Seat
    if (mySeat) {
      document.getElementById('myPlayerName').innerText = mySeat.name;
      document.getElementById('myPlayerStars').innerText = '★'.repeat(Math.max(0, mySeat.stars || 0));
      document.getElementById('myChipsDisplay').innerText = '₹' + (mySeat.chips || 0).toLocaleString();

      const myCardsRow = document.getElementById('myCardsRow');
      const myCards = mySeat.cards || [];
      myCardsRow.innerHTML = renderCard(myCards[0] || null, true) + renderCard(myCards[1] || null, true);

      // Check Turn & Action Buttons
      const isMyTurn = (table.activeTurnIndex !== -1 && seats[table.activeTurnIndex]?.id === myId);
      const callAmount = (table.currentHighBet || 0) - (mySeat.roundBet || 0);

      document.getElementById('btnCheckCall').innerText = callAmount > 0 ? ('CALL ₹' + callAmount) : 'CHECK';
      document.getElementById('btnRaise').innerText = callAmount > 0 ? 'RAISE' : 'BET';

      const actionsBar = document.getElementById('actionsBar');
      Array.from(actionsBar.children).forEach(btn => btn.disabled = !isMyTurn);

      if (isMyTurn && !window._lastTurnNotified) {
        window._lastTurnNotified = true;
        playTurnAlert();
        if (navigator.vibrate) navigator.vibrate([100, 50, 100]);
      } else if (!isMyTurn) {
        window._lastTurnNotified = false;
      }
    }

    // Showdown Winners Banner
    const winnerArea = document.getElementById('winnerBannerArea');
    if (table.phase === 'SHOWDOWN' && table.winners && table.winners.length > 0) {
      const winnerText = table.winners.map(w => w.playerName + ' WON ₹' + w.amountWon.toLocaleString() + ' (' + (w.potDescription || 'POT') + ')').join(' | ');
      winnerArea.innerHTML = '<div class="winner-banner">🏆 WINNER: ' + winnerText + '</div>';
      playWinSound();
    } else {
      winnerArea.innerHTML = '';
    }
  }

  // Start connection
  window.addEventListener('load', initSocket);
</script>
</body>
</html>
        """.trimIndent()
    }
}
