/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0.
 */

import assert from "node:assert/strict";

globalThis.window = {};
await import("./data/fund-5y.js");

const fixture = globalThis.window.FINERACT_SYNTHETIC_FUND;
assert.ok(fixture, "fixture must load");
assert.equal(fixture.meta.synthetic, true, "fixture must identify itself as synthetic");
assert.equal(fixture.months.length, 60, "fixture must contain exactly five years / 60 months");
assert.equal(fixture.meta.periodStart, fixture.months[0][0], "metadata start must match first row");
assert.equal(fixture.meta.periodEnd, fixture.months.at(-1)[0], "metadata end must match last row");

const periods = new Set();
for (const [index, row] of fixture.months.entries()) {
  assert.equal(row.length, fixture.fields.length, `row ${index + 1} must match schema width`);
  const record = Object.fromEntries(fixture.fields.map((field, fieldIndex) => [field, row[fieldIndex]]));
  assert.ok(!periods.has(record.period), `period ${record.period} must be unique`);
  periods.add(record.period);
  assert.equal(record.reconciledPayments + record.unmatchedPayments, record.payments,
    `${record.period}: reconciled + unmatched must equal total payments`);
  assert.ok(record.assets > record.loanPortfolio, `${record.period}: assets must exceed loan portfolio`);
  assert.ok(record.accounts >= record.members, `${record.period}: account population must cover members`);
  assert.ok(record.exceptions >= record.unmatchedPayments,
    `${record.period}: unmatched payments must be represented in exception count`);
}

const memberIds = new Set(fixture.members.map(member => member.id));
for (const account of fixture.accounts) {
  assert.ok(memberIds.has(account.member), `${account.id}: account must reference a seeded member`);
}
for (const loan of fixture.loans) {
  assert.ok(memberIds.has(loan.member), `${loan.id}: loan must reference a seeded member`);
}

for (const scenario of ["baseline", "provider", "backlog", "fraud", "liquidity"]) {
  assert.ok(fixture.scenarioOverlays[scenario], `scenario ${scenario} must be defined`);
}

console.log(`Verified ${fixture.meta.fixtureId}: ${fixture.months.length} months, ${fixture.members.length} member seeds, ${fixture.accounts.length} account seeds, ${fixture.loans.length} loan seeds.`);
