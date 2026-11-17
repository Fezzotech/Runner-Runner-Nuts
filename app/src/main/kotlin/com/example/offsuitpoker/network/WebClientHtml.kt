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
    background-color: #050507;
    color: #EDEDED;
    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
    min-height: 100vh;
    display: flex;
    justify-content: center;
    align-items: stretch;
    overflow-x: hidden;
    user-select: none;
    -webkit-user-select: none;
  }
  .app-container {
    width: 100%;
    max-width: 540px;
    min-height: 100vh;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    background-color: #08080A;
    border-left: 1px solid #1A1A26;
    border-right: 1px solid #1A1A26;
    box-shadow: 0 0 60px rgba(0, 0, 0, 0.85);
    position: relative;
  }
  header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 10px 14px;
    background: #0D0D12;
    border-bottom: 1px solid #1E1E28;
    z-index: 10;
  }
  .app-title { font-size: 13px; font-weight: 900; letter-spacing: 1.5px; color: #D4AF37; }
  .header-actions { display: flex; align-items: center; gap: 8px; }
  .table-badge { font-size: 11px; font-family: monospace; color: #8E8EA0; background: #151520; padding: 4px 8px; border-radius: 6px; }
  .btn-icon {
    background: #151520;
    border: 1px solid #282836;
    color: #EDEDED;
    font-size: 11px;
    padding: 4px 8px;
    border-radius: 6px;
    cursor: pointer;
    font-family: monospace;
    font-weight: 700;
  }

  /* Table Felt Arena */
  .table-container {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: space-around;
    padding: 8px 10px;
    gap: 10px;
    position: relative;
  }

  /* Top Opponents Row */
  .opponents-row {
    display: flex;
    justify-content: center;
    align-items: flex-start;
    flex-wrap: wrap;
    gap: 8px;
    min-height: 72px;
    padding: 2px;
  }
  .player-pod {
    background: #111118;
    border: 1.5px solid #232332;
    border-radius: 12px;
    padding: 6px 10px;
    min-width: 86px;
    text-align: center;
    position: relative;
    transition: all 0.25s ease;
    box-shadow: 0 3px 8px rgba(0,0,0,0.5);
  }
  .player-pod.active-turn {
    border-color: #D4AF37;
    box-shadow: 0 0 12px rgba(212, 175, 55, 0.55);
    transform: translateY(-2px);
  }
  .player-pod.folded { opacity: 0.45; filter: grayscale(80%); }
  .pod-name { font-size: 11px; font-weight: 800; color: #FFF; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 88px; }
  .pod-chips { font-size: 11px; font-weight: 800; color: #22C55E; font-family: monospace; margin-top: 2px; }
  .pod-bet {
    font-size: 10px;
    color: #D4AF37;
    font-family: monospace;
    margin-top: 3px;
    background: rgba(212, 175, 55, 0.12);
    border: 1px solid rgba(212, 175, 55, 0.35);
    border-radius: 5px;
    padding: 1px 4px;
    font-weight: 700;
  }
  .dealer-btn {
    position: absolute;
    top: -6px;
    right: -6px;
    background: #D4AF37;
    color: #08080A;
    font-size: 9px;
    font-weight: 900;
    width: 17px;
    height: 17px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    border: 1.5px solid #FFF;
    box-shadow: 0 2px 4px rgba(0,0,0,0.5);
  }

  /* Center Felt Arena */
  .poker-felt {
    background: radial-gradient(ellipse at 50% 50%, #0E2419 0%, #091710 65%, #050B08 100%);
    border: 3.5px solid #201D17;
    outline: 1.5px solid rgba(212, 175, 55, 0.45);
    border-radius: 36px;
    box-shadow: inset 0 0 35px rgba(0, 0, 0, 0.85), 0 8px 25px rgba(0, 0, 0, 0.9);
    padding: 14px 10px 16px 10px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    position: relative;
    width: 100%;
    max-width: 440px;
    margin: 0 auto;
  }
  .felt-watermark {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    font-size: 11px;
    font-weight: 900;
    letter-spacing: 4px;
    color: #D4AF37;
    opacity: 0.12;
    pointer-events: none;
    white-space: nowrap;
  }
  .felt-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    width: 100%;
    margin-bottom: 12px;
    z-index: 1;
  }
  .phase-tag {
    font-size: 9.5px;
    font-weight: 900;
    letter-spacing: 1.5px;
    color: #D4AF37;
    background: rgba(14, 14, 20, 0.85);
    border: 1px solid rgba(212, 175, 55, 0.35);
    padding: 3px 8px;
    border-radius: 12px;
    font-family: monospace;
  }
  .pot-capsule {
    background: #0E0E16;
    border: 1.5px solid #D4AF37;
    border-radius: 20px;
    padding: 4px 14px;
    display: flex;
    align-items: center;
    gap: 7px;
    box-shadow: 0 0 12px rgba(212, 175, 55, 0.25);
    z-index: 1;
  }
  .pot-chip-icon { font-size: 12px; }
  .pot-label { font-size: 9.5px; font-weight: 800; color: #A1A1AA; letter-spacing: 1px; }
  .pot-value { font-size: 16px; font-weight: 900; color: #D4AF37; font-family: monospace; }

  .community-cards {
    display: flex;
    justify-content: center;
    align-items: center;
    gap: 6px;
    width: 100%;
    min-height: 86px;
    z-index: 1;
  }

  /* Playing Cards (Genuine 4-Color Casino Styling) */
  .card {
    width: 58px;
    height: 84px;
    background: #FFFFFF;
    border-radius: 7px;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    position: relative;
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.7), inset 0 0 0 1px rgba(0, 0, 0, 0.08);
    user-select: none;
    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, monospace;
    font-weight: 900;
    padding: 3px;
    box-sizing: border-box;
    flex-shrink: 0;
  }
  .card.large {
    width: 78px;
    height: 112px;
    border-radius: 9px;
    padding: 5px;
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.85), inset 0 0 0 1px rgba(0, 0, 0, 0.08);
  }
  .card-corner {
    position: absolute;
    display: flex;
    flex-direction: column;
    align-items: center;
    line-height: 0.92;
  }
  .card-corner.top-left {
    top: 3px;
    left: 4px;
  }
  .card-corner.bottom-right {
    bottom: 3px;
    right: 4px;
    transform: rotate(180deg);
  }
  .corner-rank {
    font-size: 13px;
    font-weight: 900;
    letter-spacing: -0.5px;
  }
  .corner-suit {
    font-size: 9.5px;
    margin-top: 1px;
  }
  .card.large .corner-rank {
    font-size: 17px;
  }
  .card.large .corner-suit {
    font-size: 12px;
  }
  .card-center-suit {
    font-size: 22px;
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    line-height: 1;
  }
  .card.large .card-center-suit {
    font-size: 32px;
  }

  /* 4-Color Deck */
  .suit-h { color: #DC2626; } /* Crimson Hearts */
  .suit-d { color: #2563EB; } /* Royal Blue Diamonds */
  .suit-c { color: #16A34A; } /* Emerald Green Clubs */
  .suit-s { color: #18181B; } /* Charcoal Spades */

  /* Face-down Card Back */
  .card-back {
    background: #0E0E16;
    border: 1.5px solid #D4AF37;
    padding: 3px;
  }
  .card-back-pattern {
    width: 100%;
    height: 100%;
    border-radius: 4px;
    background: repeating-linear-gradient(45deg, #181826, #181826 4px, #0F0F1A 4px, #0F0F1A 8px);
    border: 1px solid rgba(212, 175, 55, 0.35);
    display: flex;
    align-items: center;
    justify-content: center;
    box-sizing: border-box;
  }
  .card-back-logo {
    font-size: 14px;
    color: #D4AF37;
    text-shadow: 0 0 6px rgba(212, 175, 55, 0.6);
  }
  .card.large .card-back-logo {
    font-size: 22px;
  }

  /* Winner Announcement Banner */
  .winner-banner {
    background: #14141E;
    border: 1.5px solid #D4AF37;
    border-radius: 8px;
    padding: 8px 14px;
    margin: 6px auto;
    font-size: 11.5px;
    font-weight: 800;
    color: #EDEDED;
    text-align: center;
    font-family: monospace;
    max-width: 95%;
    box-shadow: 0 0 12px rgba(212, 175, 55, 0.4);
    z-index: 2;
  }

  /* Bottom Local Player Section */
  .local-player-section {
    background: #0D0D14;
    border: 1.5px solid #1E1E2C;
    border-radius: 18px 18px 0 0;
    padding: 12px 14px;
    display: flex;
    flex-direction: column;
    gap: 8px;
    box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.7);
    padding-bottom: max(14px, env(safe-area-inset-bottom));
    transition: all 0.3s ease;
  }
  .local-player-section.my-turn {
    border-color: rgba(212, 175, 55, 0.8);
    box-shadow: 0 -4px 25px rgba(212, 175, 55, 0.3);
  }
  .local-meta {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  .local-name-stars { display: flex; align-items: center; gap: 8px; }
  .local-name { font-size: 13.5px; font-weight: 900; color: #FFF; }
  .local-stars { font-size: 11px; color: #D4AF37; letter-spacing: 1px; }
  .local-chips-info { display: flex; align-items: center; gap: 8px; }
  .local-bet-tag {
    font-size: 10px;
    font-family: monospace;
    color: #D4AF37;
    background: rgba(212, 175, 55, 0.15);
    border: 1px solid rgba(212, 175, 55, 0.35);
    border-radius: 5px;
    padding: 1px 5px;
    font-weight: 700;
  }
  .local-bankroll { font-size: 15px; font-weight: 900; color: #22C55E; font-family: monospace; }

  .local-cards-area {
    display: flex;
    justify-content: center;
    align-items: center;
    gap: 12px;
    margin: 2px 0;
  }
  .card-hole-0 { transform: rotate(-3deg); }
  .card-hole-1 { transform: rotate(3deg); }

  /* Turn Status Indicator */
  .turn-status-banner {
    text-align: center;
    font-size: 10.5px;
    font-weight: 800;
    font-family: monospace;
    letter-spacing: 1px;
    color: #8E8EA0;
    min-height: 14px;
  }
  .turn-status-banner.active { color: #D4AF37; }

  /* Action Buttons Bar */
  .actions-bar {
    display: flex;
    gap: 8px;
    width: 100%;
  }
  .action-btn {
    flex: 1;
    padding: 13px 4px;
    border-radius: 10px;
    font-weight: 900;
    font-size: 12.5px;
    font-family: monospace;
    cursor: pointer;
    text-transform: uppercase;
    transition: all 0.15s ease;
    letter-spacing: 0.5px;
  }
  .action-btn:active { transform: scale(0.97); }
  .action-btn:disabled { opacity: 0.3; cursor: not-allowed; filter: grayscale(40%); }
  .btn-fold { background: #1C1014; color: #EF4444; border: 1.5px solid #7F1D1D; }
  .btn-check-call { background: #0E2316; color: #22C55E; border: 1.5px solid #166534; }
  .btn-check-call.turn-glow {
    border-color: #22C55E;
    box-shadow: 0 0 14px rgba(34, 197, 94, 0.45);
    animation: pulseGreen 1.4s infinite alternate;
  }
  @keyframes pulseGreen {
    from { box-shadow: 0 0 6px rgba(34, 197, 94, 0.3); }
    to { box-shadow: 0 0 16px rgba(34, 197, 94, 0.7); }
  }
  .btn-bet-raise { background: #D4AF37; color: #0A0A0E; border: 1.5px solid #F59E0B; }
  .btn-allin { background: #25120D; color: #F97316; border: 1.5px solid #9A3412; }

  /* Raise Controls Drawer */
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
    padding: 6px 2px;
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
<div class="app-container">

<header>
  <div class="app-title">RUNNER RUNNER NUTS</div>
  <div class="header-actions">
    <button class="btn-icon" onclick="openNameModal()">👤 NAME</button>
    <div class="table-badge" id="handBadge">HAND #1</div>
  </div>
</header>

<div class="table-container">
  <!-- Top Opponents Row -->
  <div class="opponents-row" id="opponentsRow">
    <div style="font-size: 11px; color: #555; margin-top: 15px;">Waiting for table state...</div>
  </div>

  <!-- Center Poker Felt Arena -->
  <div class="poker-felt">
    <div class="felt-watermark">RUNNER RUNNER NUTS</div>

    <div class="felt-header">
      <div class="phase-tag" id="phaseTag">PRE-FLOP</div>
      <div class="pot-capsule">
        <span class="pot-chip-icon">🪙</span>
        <span class="pot-label">TOTAL POT</span>
        <span class="pot-value" id="potDisplay">₹0</span>
      </div>
      <div style="width: 45px;"></div>
    </div>

    <div class="community-cards" id="communityCardsRow">
      <!-- 5 community cards -->
    </div>

    <div id="winnerBannerArea" style="width: 100%;"></div>
  </div>

  <!-- Bottom: Local Player Area -->
  <div class="local-player-section" id="localPlayerSection">
    <div class="local-meta">
      <div class="local-name-stars">
        <span class="local-name" id="myPlayerName">Player</span>
        <span class="local-stars" id="myPlayerStars">★★★★★</span>
      </div>
      <div class="local-chips-info">
        <span class="local-bet-tag" id="myBetTag" style="display: none;">BET: ₹0</span>
        <span class="local-bankroll" id="myChipsDisplay">₹10,000</span>
      </div>
    </div>

    <div class="local-cards-area">
      <div id="myCardsRow" style="display: flex; gap: 10px;">
        <!-- 2 Hole Cards -->
      </div>
    </div>

    <div class="turn-status-banner" id="turnStatusBanner">WAITING FOR ACTION...</div>

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

</div><!-- /.app-container -->

<!-- Join / Change Name Modal Dialog -->
<div class="modal-overlay" id="joinModal">
  <div class="modal-card">
    <div class="modal-title">RUNNER RUNNER NUTS</div>
    <div class="modal-desc">Enter your player name to join the live Texas Hold'em table from any browser!</div>
    <input type="text" class="input-field" id="nameInput" placeholder="Your Name" maxlength="12" value="Web Player">
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
  let currencySym = '₹';

  // Sound Synthesizer via Web Audio API
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

  function formatChips(amt) {
    return currencySym + (amt || 0).toLocaleString();
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

  function openNameModal() {
    document.getElementById('joinModal').style.display = 'flex';
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
    document.getElementById('sliderValueText').innerText = formatChips(slider.value);
  }

  document.getElementById('raiseSlider').addEventListener('input', (e) => {
    document.getElementById('sliderValueText').innerText = formatChips(e.target.value);
  });

  function updateSliderBounds() {
    if (!currentTable || !mySeat) return;
    const minRaise = (currentTable.currentHighBet || 0) + (currentTable.minRaiseAmount || currentTable.bigBlind || 100);
    const maxRaise = mySeat.chips + (mySeat.roundBet || 0);
    const slider = document.getElementById('raiseSlider');
    slider.min = Math.min(minRaise, maxRaise);
    slider.max = maxRaise;
    slider.value = slider.min;
    document.getElementById('sliderValueText').innerText = formatChips(slider.value);
  }

  const rankMap = {
    'TWO': '2', 'THREE': '3', 'FOUR': '4', 'FIVE': '5', 'SIX': '6',
    'SEVEN': '7', 'EIGHT': '8', 'NINE': '9', 'TEN': '10',
    'JACK': 'J', 'QUEEN': 'Q', 'KING': 'K', 'ACE': 'A',
    '2':'2', '3':'3', '4':'4', '5':'5', '6':'6', '7':'7', '8':'8', '9':'9', '10':'10',
    'J':'J', 'Q':'Q', 'K':'K', 'A':'A'
  };

  function getCardRankStr(card) {
    if (!card) return '?';
    let r = card.rank;
    if (typeof r === 'object' && r !== null) {
      r = r.display || r.name || r.value || '?';
    }
    return rankMap[r] || r || '?';
  }

  function getCardSuitInfo(card) {
    if (!card || !card.suit) return { icon: '♠', css: 'suit-s' };
    let s = (typeof card.suit === 'object' && card.suit !== null) ? (card.suit.name || card.suit.symbol || '') : card.suit;
    s = (s + '').toUpperCase();
    if (s.startsWith('H') || s === '♥') return { icon: '♥', css: 'suit-h' };
    if (s.startsWith('D') || s === '♦') return { icon: '♦', css: 'suit-d' };
    if (s.startsWith('C') || s === '♣') return { icon: '♣', css: 'suit-c' };
    return { icon: '♠', css: 'suit-s' };
  }

  function renderCard(card, isLarge, extraClass) {
    const cls = extraClass ? (' ' + extraClass) : '';
    if (!card) {
      return '<div class="card card-back' + (isLarge ? ' large' : '') + cls + '">' +
             '  <div class="card-back-pattern">' +
             '    <span class="card-back-logo">♠</span>' +
             '  </div>' +
             '</div>';
    }
    const rank = getCardRankStr(card);
    const suit = getCardSuitInfo(card);

    return '<div class="card ' + suit.css + (isLarge ? ' large' : '') + cls + '">' +
           '  <div class="card-corner top-left">' +
           '    <span class="corner-rank">' + rank + '</span>' +
           '    <span class="corner-suit">' + suit.icon + '</span>' +
           '  </div>' +
           '  <div class="card-center-suit">' + suit.icon + '</div>' +
           '  <div class="card-corner bottom-right">' +
           '    <span class="corner-rank">' + rank + '</span>' +
           '    <span class="corner-suit">' + suit.icon + '</span>' +
           '  </div>' +
           '</div>';
  }

  function renderTable(table) {
    currentTable = table;
    currencySym = table.currencySymbol || '₹';

    document.getElementById('handBadge').innerText = 'HAND #' + (table.handCount || 1);
    document.getElementById('phaseTag').innerText = (table.phase || 'PRE-FLOP').replace('_', ' ');

    // Calculate Live Total Pot including active player round bets
    const currentBets = (table.seats || []).reduce((sum, s) => sum + (s.roundBet || 0), 0);
    const sidePotsTotal = (table.sidePots || []).reduce((sum, sp) => sum + (sp.amount || 0), 0);
    const totalPot = (table.mainPot || 0) + sidePotsTotal + currentBets;
    document.getElementById('potDisplay').innerText = formatChips(totalPot);

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
                 '  <div class="pod-chips">' + formatChips(op.chips) + '</div>' +
                 (op.roundBet > 0 ? '<div class="pod-bet">Bet: ' + formatChips(op.roundBet) + '</div>' : '') +
                 '</div>';
    });
    oppRow.innerHTML = oppHtml || '<div style="font-size: 11px; color: #555;">Waiting for opponents...</div>';

    // Render Local Seat
    if (mySeat) {
      document.getElementById('myPlayerName').innerText = mySeat.name;
      document.getElementById('myPlayerStars').innerText = '★'.repeat(Math.max(0, mySeat.stars || 0));
      document.getElementById('myChipsDisplay').innerText = formatChips(mySeat.chips);

      const myBetTag = document.getElementById('myBetTag');
      if (mySeat.roundBet > 0) {
        myBetTag.style.display = 'inline-block';
        myBetTag.innerText = 'BET: ' + formatChips(mySeat.roundBet);
      } else {
        myBetTag.style.display = 'none';
      }

      const myCardsRow = document.getElementById('myCardsRow');
      const myCards = mySeat.cards || [];
      myCardsRow.innerHTML = renderCard(myCards[0] || null, true, 'card-hole-0') +
                             renderCard(myCards[1] || null, true, 'card-hole-1');

      // Check Turn & Action Buttons
      const isMyTurn = (table.activeTurnIndex !== -1 && seats[table.activeTurnIndex]?.id === myId);
      const callAmount = (table.currentHighBet || 0) - (mySeat.roundBet || 0);

      const checkCallBtn = document.getElementById('btnCheckCall');
      checkCallBtn.innerText = callAmount > 0 ? ('CALL ' + formatChips(callAmount)) : 'CHECK';
      document.getElementById('btnRaise').innerText = callAmount > 0 ? 'RAISE' : 'BET';

      const localSection = document.getElementById('localPlayerSection');
      const turnBanner = document.getElementById('turnStatusBanner');

      if (isMyTurn) {
        localSection.classList.add('my-turn');
        checkCallBtn.classList.add('turn-glow');
        turnBanner.classList.add('active');
        turnBanner.innerText = '⚡ YOUR TURN TO ACT';
      } else {
        localSection.classList.remove('my-turn');
        checkCallBtn.classList.remove('turn-glow');
        turnBanner.classList.remove('active');
        const activePlayer = table.activeTurnIndex !== -1 ? seats[table.activeTurnIndex] : null;
        turnBanner.innerText = activePlayer ? ('WAITING FOR ' + activePlayer.name.toUpperCase() + '...') : 'WAITING...';
      }

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
      const winnerText = table.winners.map(w => w.playerName + ' WON ' + formatChips(w.amountWon) + ' (' + (w.potDescription || 'POT') + ')').join(' | ');
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
