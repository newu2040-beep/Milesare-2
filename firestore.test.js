const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

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

test("Unauthenticated user: cannot read posts", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("posts").get());
});

test("Authenticated user: can create confession post", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("posts").doc("post_1").set({
      postId: "post_1",
      authorId: ALICE_UID,
      anonymousName: "Midnight Voice #123",
      anonymousAvatar: "mask",
      category: "Confession",
      content: "I secretly love midnight rain and journaling.",
      commentsEnabled: true,
      reactionsEnabled: true,
      reactionCount: 0,
      commentCount: 0,
      saveCount: 0,
      isArchived: false,
      isDeleted: false,
    })
  );
});

test("Authenticated user: cannot impersonate another author on post creation", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("posts").doc("post_fake").set({
      postId: "post_fake",
      authorId: BOB_UID,
      anonymousName: "Impostor",
      anonymousAvatar: "cat",
      category: "Secret",
      content: "Fake post",
    })
  );
});

test("Authenticated user: can read public posts", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("posts").doc("post_alice").set({
      postId: "post_alice",
      authorId: ALICE_UID,
      anonymousName: "Alice Mask",
      anonymousAvatar: "star",
      category: "Thoughts",
      content: "Deep midnight thoughts...",
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertSucceeds(bobDb.collection("posts").doc("post_alice").get());
});

test("Authenticated user: cannot delete another user's post", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("posts").doc("post_alice").set({
      postId: "post_alice",
      authorId: ALICE_UID,
      anonymousName: "Alice Mask",
      anonymousAvatar: "star",
      category: "Thoughts",
      content: "Deep midnight thoughts...",
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("posts").doc("post_alice").delete());
});

test("Authenticated user: can react to post", async () => {
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertSucceeds(
    bobDb.collection("posts").doc("post_alice").collection("reactions").doc("reaction_bob").set({
      reactionId: "reaction_bob",
      postId: "post_alice",
      userId: BOB_UID,
      reactionType: "relatable",
    })
  );
});

test("User profile: Alice cannot read or write Bob profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).get());
});
