const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_hoa_101";
const BOB_UID = "bob_hoa_202";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function pastDate(msOffset = 5000) {
  return new Date(Date.now() - msOffset);
}

function validCountryPayload(countryId = 101) {
  return {
    countryId,
    worldScope: "AFRICA",
    name: "Nigeria",
    isoCode: "NGA",
    flagEmoji: "NG",
    capitalCity: "Abuja",
    currencyCode: "NGN",
    currencyName: "Nigerian Naira",
    parliamentName: "National Assembly",
    parliamentSeats: 469,
    population: 223800000,
    infrastructureIndex: 64,
    incomeTaxRatePct: 18.0,
    corporateTaxRatePct: 25.0,
    salesTaxRatePct: 7.5,
    immigrationRenewalYears: 10,
    naturalResources: ["Crude Oil", "Natural Gas", "Tin", "Limestone"],
    industries: ["Energy", "Agriculture", "Telecommunications", "Banking & Finance"],
    treasuryStatus: "Treasury Engine Pending Phase 9",
    activePartiesCount: 0,
    createdAt: pastDate(10000),
  };
}

function validStatePayload(stateId = 201, countryId = 101) {
  return {
    stateId,
    countryId,
    name: "Lagos State",
    capitalCity: "Ikeja",
    governorTitle: "Governor of Lagos State",
    departments: ["Finance", "Trade", "Transport", "Infrastructure", "Health", "Education"],
    stateTaxRatePct: 4.5,
    infrastructureIndex: 76,
    population: 16500000,
    economicFocus: "Maritime Trade, Finance & Technology",
    treasuryStatus: "State Treasury Pending Phase 9",
    createdAt: pastDate(8000),
  };
}

function validCountyPayload(countyId = 301, stateId = 201, countryId = 101) {
  return {
    countyId,
    stateId,
    countryId,
    name: "Eti-Osa County",
    settlements: ["Victoria Island", "Ikoyi", "Lekki Phase 1"],
    localServices: ["Port Authority", "Municipal Transit", "County Registry", "Clinics"],
    infrastructureIndex: 79,
    population: 1450000,
    registeredBusinesses: 0,
    marketplaceStatus: "TSN Exchange Pending Phase 8",
    economicSummary: "Coastal financial & commercial district",
    createdAt: pastDate(6000),
  };
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated users cannot read or write any House of Africa collection", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("countries").where("worldScope", "==", "AFRICA").get());
  await assertFails(unauthDb.collection("states").where("countryId", "==", 101).get());
  await assertFails(unauthDb.collection("counties").where("stateId", "==", 201).get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    unauthDb.collection("countries").doc("101").set(validCountryPayload(101))
  );
});

test("User account enforces strict ownership, immutable role, and schema validation", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const created = pastDate(10000);

  // Valid create succeeds
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Amina Okonkwo",
      role: "citizen",
      activeCountryId: 0,
      activeStateId: 0,
      activeCountyId: 0,
      createdAt: created,
      updatedAt: created,
    })
  );

  // Role privilege escalation fails
  await assertFails(
    bobDb.collection("users").doc(BOB_UID).set({
      userId: BOB_UID,
      displayName: "Bob Escalator",
      role: "admin",
      activeCountryId: 0,
      activeStateId: 0,
      activeCountyId: 0,
      createdAt: created,
      updatedAt: created,
    })
  );

  // Shadow extra field fails
  await assertFails(
    bobDb.collection("users").doc(BOB_UID).set({
      userId: BOB_UID,
      displayName: "Bob Ghost",
      role: "citizen",
      activeCountryId: 0,
      activeStateId: 0,
      activeCountyId: 0,
      isVerified: true,
      createdAt: created,
      updatedAt: created,
    })
  );

  // Bob cannot read or update Alice's account
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).update({
      displayName: "Hijacked",
      updatedAt: pastDate(1000),
    })
  );

  // Valid partial update by Alice succeeds
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      activeCountryId: 101,
      activeStateId: 201,
      activeCountyId: 301,
      updatedAt: pastDate(1000),
    })
  );

  // Mutating immutable createdAt or role fails
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      createdAt: pastDate(500),
      updatedAt: pastDate(500),
    })
  );
});

test("World geography hierarchy enforces parent existence, immutable IDs, and filtered queries", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  // Orphaned state before parent country exists must fail
  await assertFails(
    aliceDb.collection("states").doc("201").set(validStatePayload(201, 101))
  );

  // Create country -> state -> county in order succeeds
  await assertSucceeds(
    aliceDb.collection("countries").doc("101").set(validCountryPayload(101))
  );
  await assertSucceeds(
    aliceDb.collection("states").doc("201").set(validStatePayload(201, 101))
  );
  await assertSucceeds(
    aliceDb.collection("counties").doc("301").set(validCountyPayload(301, 201, 101))
  );

  // Non-admin players cannot update or delete immutable world IDs
  await assertFails(
    bobDb.collection("countries").doc("101").update({ name: "Modified" })
  );
  await assertFails(bobDb.collection("countries").doc("101").delete());
  await assertFails(bobDb.collection("states").doc("201").delete());
  await assertFails(bobDb.collection("counties").doc("301").delete());

  // Doc ID mismatch against numeric field fails
  await assertFails(
    aliceDb.collection("countries").doc("999").set(validCountryPayload(102))
  );

  // Filtered Kotlin-mirrored queries succeed; unfiltered countries query fails
  await assertSucceeds(
    bobDb.collection("countries").where("worldScope", "==", "AFRICA").get()
  );
  await assertSucceeds(
    bobDb.collection("states").where("countryId", "==", 101).get()
  );
  await assertSucceeds(
    bobDb.collection("counties").where("stateId", "==", 201).get()
  );
  await assertFails(bobDb.collection("countries").get());
});

test("Internal relocation log validates existing geography and prevents tampering", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  await assertSucceeds(
    aliceDb.collection("countries").doc("101").set(validCountryPayload(101))
  );
  await assertSucceeds(
    aliceDb.collection("states").doc("201").set(validStatePayload(201, 101))
  );
  await assertSucceeds(
    aliceDb.collection("counties").doc("301").set(validCountyPayload(301, 201, 101))
  );

  // Valid settlement log succeeds
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("relocations").doc("reloc_1").set({
      countryId: 101,
      fromStateId: 0,
      fromCountyId: 0,
      toStateId: 201,
      toCountyId: 301,
      relocationType: "INITIAL_SETTLEMENT",
      note: "Initial settlement in Eti-Osa County, Lagos State",
      createdAt: pastDate(1000),
    })
  );

  // Future timestamp fails
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("relocations").doc("reloc_future").set({
      countryId: 101,
      fromStateId: 0,
      fromCountyId: 0,
      toStateId: 201,
      toCountyId: 301,
      relocationType: "INITIAL_SETTLEMENT",
      note: "Tampered future timestamp",
      createdAt: new Date(Date.now() + 60000),
    })
  );

  // Non-existent county ID fails
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("relocations").doc("reloc_bad").set({
      countryId: 101,
      fromStateId: 201,
      fromCountyId: 301,
      toStateId: 201,
      toCountyId: 399,
      relocationType: "INTERNAL_RELOCATION",
      note: "Non-existent destination county",
      createdAt: pastDate(1000),
    })
  );

  // Audit records cannot be updated or deleted, nor read by other users
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("relocations").doc("reloc_1").update({
      note: "Tampered",
    })
  );
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("relocations").doc("reloc_1").delete()
  );
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("relocations").get()
  );
});

test("Server clock and immutable tick_logs prevent rollbacks and deletion", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = pastDate(2000);

  await assertSucceeds(
    aliceDb.collection("world_clock").doc("canonical").set({
      scope: "WORLD",
      eraNumber: 1,
      eraStartTime: pastDate(500000),
      lastTickTime: now,
      lastProcessedTickIndex: 5,
      totalGameDaysElapsed: 5.5,
    })
  );

  // Rolling back tick index fails
  await assertFails(
    aliceDb.collection("world_clock").doc("canonical").update({
      eraNumber: 1,
      eraStartTime: pastDate(500000),
      lastTickTime: pastDate(500),
      lastProcessedTickIndex: 3,
      totalGameDaysElapsed: 3.0,
    })
  );

  // Tick log create succeeds, update/delete fail
  const tickRef = aliceDb.collection("world_clock").doc("canonical").collection("tick_logs").doc("era1_tick_6");
  await assertSucceeds(
    tickRef.set({
      scope: "CLOCK_TICK",
      tickIndex: 6,
      gameDaysAtTick: 6.0,
      eventsProcessed: "Daily tick batch",
      processedByUid: ALICE_UID,
      processedAt: now,
    })
  );
  await assertFails(tickRef.update({ eventsProcessed: "Tampered" }));
  await assertFails(tickRef.delete());
});

test("Player identities require permanent ID >= 12453 and enforce soft delete instead of document deletion", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = pastDate(2000);

  // Too low playerId fails
  await assertFails(
    aliceDb.collection("players").doc("100").set({
      playerId: 100,
      ownerUid: ALICE_UID,
      characterRole: "MAIN",
      fullName: "Bad Id",
      genderPresentation: "Woman",
      dateOfBirth: "Yr 1, Jan 1",
      nationalityCountryId: 101,
      placeOfBirth: "Abuja",
      currentCountryId: 101,
      currentStateId: 201,
      currentCountyId: 301,
      occupation: "Trader",
      biography: "Bio",
      avatarStyle: "ROYAL_KENTE",
      avatarAccentIndex: 0,
      accountStatus: "ACTIVE",
      publicVisible: true,
      createdAt: now,
      updatedAt: now,
    })
  );

  const validRef = aliceDb.collection("players").doc("12453");
  await assertSucceeds(
    validRef.set({
      playerId: 12453,
      ownerUid: ALICE_UID,
      characterRole: "MAIN",
      fullName: "Amina Okonkwo",
      genderPresentation: "Woman",
      dateOfBirth: "Yr 1, Jan 1",
      nationalityCountryId: 101,
      placeOfBirth: "Lagos",
      currentCountryId: 101,
      currentStateId: 201,
      currentCountyId: 301,
      occupation: "Economist",
      biography: "Builder",
      avatarStyle: "ROYAL_KENTE",
      avatarAccentIndex: 1,
      accountStatus: "ACTIVE",
      publicVisible: true,
      createdAt: now,
      updatedAt: now,
    })
  );

  // Bob cannot soft-delete Alice or delete doc; Alice cannot hard-delete either
  await assertFails(bobDb.collection("players").doc("12453").update({ accountStatus: "DELETED" }));
  await assertFails(validRef.delete());

  // Soft delete succeeds by owner
  await assertSucceeds(
    validRef.update({
      accountStatus: "DELETED",
      updatedAt: pastDate(500),
    })
  );
});

test("Banking ledger blocks negative balances, self transfers, and unauthorized treasury debits", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = pastDate(2000);

  await assertSucceeds(
    aliceDb.collection("bank_accounts").doc("acct_alice_personal").set({
      ownerUid: ALICE_UID,
      ownerEntityId: ALICE_UID,
      accountType: "PERSONAL",
      accountCategory: "CITIZEN",
      currencyCode: "NGN",
      balanceCents: 100000,
      restricted: false,
      createdAt: now,
      updatedAt: now,
    })
  );

  // Negative balance fails
  await assertFails(
    aliceDb.collection("bank_accounts").doc("acct_alice_bad").set({
      ownerUid: ALICE_UID,
      ownerEntityId: ALICE_UID,
      accountType: "PERSONAL",
      accountCategory: "CITIZEN",
      currencyCode: "NGN",
      balanceCents: -10,
      restricted: false,
      createdAt: now,
      updatedAt: now,
    })
  );

  // Bob cannot debit Alice's personal account
  await assertFails(
    bobDb.collection("bank_accounts").doc("acct_alice_personal").update({
      balanceCents: 0,
      updatedAt: pastDate(500),
    })
  );

  // Self-to-self ledger transaction fails; valid distinct accounts succeed and remain immutable
  await assertFails(
    aliceDb.collection("ledger_transactions").doc("tx_self").set({
      txRef: "TX-SELF",
      initiatedByUid: ALICE_UID,
      senderAccountId: "acct_alice_personal",
      receiverAccountId: "acct_alice_personal",
      amountCents: 1000,
      taxAmountCents: 0,
      txType: "TRANSFER",
      gameDay: 1.0,
      reference: "Invalid self",
      status: "SETTLED",
      publicAudit: false,
      createdAt: now,
    })
  );

  const txRef = aliceDb.collection("ledger_transactions").doc("tx_ok_1");
  await assertSucceeds(
    txRef.set({
      txRef: "TX-OK-1",
      initiatedByUid: ALICE_UID,
      senderAccountId: "acct_alice_personal",
      receiverAccountId: "acct_alice_capital",
      amountCents: 5000,
      taxAmountCents: 0,
      txType: "INTERNAL_CAPITAL_ALLOCATION",
      gameDay: 1.0,
      reference: "Capital transfer",
      status: "SETTLED",
      publicAudit: false,
      createdAt: now,
    })
  );
  await assertFails(txRef.update({ amountCents: 999999 }));
  await assertFails(txRef.delete());

  // Caretaker treasury disbursement works when government leaderUid == CARETAKER_NPC
  await assertSucceeds(
    aliceDb.collection("governments").doc("gov_country_101").set({
      scope: "GOVERNMENT",
      govLevel: "NATIONAL",
      jurisdictionId: 101,
      countryId: 101,
      name: "Federal Republic of Nigeria",
      leaderUid: "CARETAKER_NPC",
      leaderName: "Caretaker Administration",
      treasuryAccountId: "treasury_country_101",
      treasuryBalanceCents: 5000000,
      primaryTaxRatePct: 18.0,
      corporateTaxRatePct: 25.0,
      salesTaxRatePct: 7.5,
      updatedAt: now,
    })
  );
  await assertSucceeds(
    aliceDb.collection("bank_accounts").doc("treasury_country_101").set({
      ownerUid: ALICE_UID,
      ownerEntityId: "gov_country_101",
      accountType: "NATIONAL_TREASURY",
      accountCategory: "TREASURY",
      currencyCode: "NGN",
      balanceCents: 5000000,
      restricted: false,
      createdAt: now,
      updatedAt: now,
    })
  );
  await assertSucceeds(
    bobDb.collection("bank_accounts").doc("treasury_country_101").update({
      balanceCents: 4990000,
      updatedAt: pastDate(200),
    })
  );
});

test("Facilities enforce NPC workforce cap <= maxWorkforce and TSN trades block self-wash", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = pastDate(2000);

  await assertSucceeds(
    aliceDb.collection("companies").doc("501").set({
      scope: "COMPANY",
      companyId: 501,
      ownerUid: ALICE_UID,
      ceoUid: ALICE_UID,
      name: "Nile Agro Co",
      sector: "Farming",
      ownershipType: "PRIVATE",
      totalShares: 1000000,
      sharePriceCents: 1000,
      createdAt: now,
    })
  );

  // Over-capacity NPC count fails
  await assertFails(
    aliceDb.collection("companies").doc("501").collection("facilities").doc("fac_bad").set({
      companyId: 501,
      ownerUid: ALICE_UID,
      facilityName: "Overstaffed Farm",
      npcEmployees: 25,
      maxWorkforce: 20,
    })
  );

  // Within capacity succeeds
  await assertSucceeds(
    aliceDb.collection("companies").doc("501").collection("facilities").doc("fac_ok").set({
      companyId: 501,
      ownerUid: ALICE_UID,
      facilityName: "Compliant Farm",
      npcEmployees: 20,
      maxWorkforce: 20,
    })
  );

  // Circular self-trade on TSN marketplace fails
  await assertFails(
    aliceDb.collection("trade_history").doc("trade_wash").set({
      scope: "TSN_TRADE",
      buyerEntityId: "company_501",
      sellerEntityId: "company_501",
      commodity: "Maize & Grain",
      quantity: 10,
      totalCents: 10000,
      createdAt: now,
    })
  );

  // Distinct buyer and seller trade succeeds
  await assertSucceeds(
    aliceDb.collection("trade_history").doc("trade_ok").set({
      scope: "TSN_TRADE",
      buyerEntityId: "user_bob",
      sellerEntityId: "company_501",
      commodity: "Maize & Grain",
      quantity: 10,
      totalCents: 10000,
      createdAt: now,
    })
  );
});

test("Elections, Referendums, and Legal Retention Records enforce single vote and immutability", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = pastDate(2000);

  // Ballot under Bob's UID cannot be forged by Alice
  await assertFails(
    aliceDb.collection("elections").doc("801").collection("ballots").doc(BOB_UID).set({
      electionId: 801,
      voterUid: BOB_UID,
      candidate: "Amina",
      createdAt: now,
    })
  );

  const aliceBallot = aliceDb.collection("elections").doc("801").collection("ballots").doc(ALICE_UID);
  await assertSucceeds(
    aliceBallot.set({
      electionId: 801,
      voterUid: ALICE_UID,
      candidate: "Amina",
      createdAt: now,
    })
  );
  await assertFails(aliceBallot.update({ candidate: "Other" }));

  // Referendum single vote & immutable legal retention record
  const aliceVote = aliceDb.collection("referendums").doc("era_1").collection("votes").doc(ALICE_UID);
  await assertSucceeds(
    aliceVote.set({
      eraNumber: 1,
      voterUid: ALICE_UID,
      choice: "PARTIAL_RESET",
      createdAt: now,
    })
  );
  await assertFails(aliceVote.delete());

  const legalRef = aliceDb.collection("legal_retention_records").doc("ret_era_1");
  await assertSucceeds(
    legalRef.set({
      ownerUid: ALICE_UID,
      eraNumber: 1,
      decision: "CONTINUE_ERA",
      createdAt: now,
    })
  );
  await assertFails(bobDb.collection("legal_retention_records").doc("ret_era_1").get());
  await assertFails(legalRef.update({ decision: "TAMPERED" }));
  await assertFails(legalRef.delete());
});

test("Username reservations enforce global uniqueness, format regex, and immutability", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const now = pastDate(2000);

  // Unauthenticated reads/writes fail
  await assertFails(unauthDb.collection("usernames").doc("amina_ng").get());

  // Invalid lowercase mismatch or invalid underscore placement fail
  await assertFails(
    aliceDb.collection("usernames").doc("Amina_NG").set({
      username: "Amina_NG",
      usernameLower: "Amina_NG",
      ownerUid: ALICE_UID,
      createdAt: now,
    })
  );
  await assertFails(
    aliceDb.collection("usernames").doc("_amina").set({
      username: "_amina",
      usernameLower: "_amina",
      ownerUid: ALICE_UID,
      createdAt: now,
    })
  );
  await assertFails(
    aliceDb.collection("usernames").doc("amina__ng").set({
      username: "amina__ng",
      usernameLower: "amina__ng",
      ownerUid: ALICE_UID,
      createdAt: now,
    })
  );

  // Valid reservation succeeds
  await assertSucceeds(
    aliceDb.collection("usernames").doc("amina_ng").set({
      username: "Amina_NG",
      usernameLower: "amina_ng",
      ownerUid: ALICE_UID,
      createdAt: now,
    })
  );

  // Bob cannot claim the same username or delete/update Alice's username
  await assertFails(
    bobDb.collection("usernames").doc("amina_ng").set({
      username: "amina_ng",
      usernameLower: "amina_ng",
      ownerUid: BOB_UID,
      createdAt: now,
    })
  );
  await assertFails(bobDb.collection("usernames").doc("amina_ng").delete());
  await assertFails(aliceDb.collection("usernames").doc("amina_ng").update({ username: "other" }));
});


