#!/usr/bin/env python3
"""
Population script for ConspiracyCorner database.
Generates 100 users, 100 theories, 100 comments, and 100 votes using Faker.
"""

import os
import time
import random
from datetime import datetime, timedelta
from faker import Faker
import psycopg2
from psycopg2.extras import execute_values
import bcrypt

# Initialize Faker
fake = Faker()
Faker.seed(42)
random.seed(42)

# Database connection settings
DB_CONFIG = {
    'host': os.getenv('DB_HOST', 'db'),
    'port': os.getenv('DB_PORT', '5432'),
    'database': os.getenv('DB_NAME', 'conspiracy_forum'),
    'user': os.getenv('DB_USER', 'postgres'),
    'password': os.getenv('DB_PASSWORD', 'postgres')
}

# Constants
NUM_USERS = 100
NUM_THEORIES = 100
NUM_COMMENTS = 100
NUM_VOTES = 100

# Enums matching the Java entities
USER_ROLES = ['USER', 'ADMIN']
THEORY_STATUSES = ['UNVERIFIED', 'DEBUNKED', 'CONFIRMED']

# Conspiracy-themed content generators
CONSPIRACY_TOPICS = [
    "moon landing", "flat earth", "illuminati", "reptilians", "chemtrails",
    "area 51", "bigfoot", "ancient aliens", "new world order", "deep state",
    "5G towers", "vaccine microchips", "simulation theory", "time travelers",
    "hollow earth", "mandela effect", "MK Ultra", "secret societies",
    "crop circles", "bermuda triangle", "roswell", "men in black",
    "fluoride mind control", "HAARP weather control", "shadow government",
    "subliminal messages", "clone replacements", "underground bases",
    "fake celebrities", "hidden technology", "suppressed cures"
]

CONSPIRACY_VERBS = [
    "is hiding", "controls", "manipulates", "secretly funds", "covers up",
    "orchestrates", "monitors", "experiments with", "suppresses", "invented"
]

CONSPIRACY_SUBJECTS = [
    "The government", "Big pharma", "The elite", "NASA", "The CIA",
    "Silicon Valley", "The media", "The UN", "Ancient civilizations",
    "Time travelers", "Aliens", "Secret societies", "The Vatican",
    "Royal families", "Tech billionaires", "The military industrial complex"
]


def wait_for_db(max_retries=30, delay=2):
    """Wait for the database to be available."""
    print(f"Waiting for database at {DB_CONFIG['host']}:{DB_CONFIG['port']}...")
    
    for attempt in range(max_retries):
        try:
            conn = psycopg2.connect(**DB_CONFIG)
            conn.close()
            print("Database is ready!")
            return True
        except psycopg2.OperationalError as e:
            print(f"Attempt {attempt + 1}/{max_retries}: Database not ready yet... ({e})")
            time.sleep(delay)
    
    raise Exception("Could not connect to database after maximum retries")


def hash_password(password: str) -> str:
    """Hash a password using bcrypt (same as Spring Security)."""
    salt = bcrypt.gensalt(rounds=10)
    return bcrypt.hashpw(password.encode('utf-8'), salt).decode('utf-8')


def generate_conspiracy_title():
    """Generate a conspiracy-themed title."""
    templates = [
        f"The Truth About {random.choice(CONSPIRACY_TOPICS).title()}",
        f"Why {random.choice(CONSPIRACY_SUBJECTS)} {random.choice(CONSPIRACY_VERBS)} {random.choice(CONSPIRACY_TOPICS)}",
        f"{random.choice(CONSPIRACY_TOPICS).title()}: What They Don't Want You to Know",
        f"EXPOSED: {random.choice(CONSPIRACY_SUBJECTS)} and {random.choice(CONSPIRACY_TOPICS).title()}",
        f"The {random.choice(CONSPIRACY_TOPICS).title()} Conspiracy Revealed",
        f"Wake Up: {random.choice(CONSPIRACY_TOPICS).title()} Is Real",
        f"I Have Proof of {random.choice(CONSPIRACY_TOPICS).title()}",
        f"{random.choice(CONSPIRACY_SUBJECTS)} Doesn't Want You to See This",
    ]
    return random.choice(templates)


def generate_conspiracy_content():
    """Generate conspiracy-themed content."""
    intro_templates = [
        f"I've been researching {random.choice(CONSPIRACY_TOPICS)} for years, and what I found will shock you.",
        f"They don't want you to know this, but {random.choice(CONSPIRACY_SUBJECTS).lower()} has been lying to us.",
        f"Open your eyes, people. The evidence is everywhere.",
        f"My sources inside {random.choice(CONSPIRACY_SUBJECTS).lower()} have confirmed what we suspected all along.",
        f"After connecting all the dots, the truth is undeniable.",
    ]
    
    middle_templates = [
        f"Think about it - why would {random.choice(CONSPIRACY_SUBJECTS).lower()} hide this if it wasn't true?",
        f"The mainstream media won't cover this because they're controlled by {random.choice(CONSPIRACY_SUBJECTS).lower()}.",
        f"I've compiled evidence from multiple sources that proves {random.choice(CONSPIRACY_TOPICS)} is connected to {random.choice(CONSPIRACY_TOPICS)}.",
        f"Follow the money and you'll see that {random.choice(CONSPIRACY_SUBJECTS).lower()} benefits from keeping us in the dark.",
        fake.paragraph(nb_sentences=5),
    ]
    
    conclusion_templates = [
        "Wake up, sheeple!",
        "Do your own research. The truth is out there.",
        "Share this before they take it down!",
        "They can silence me, but they can't silence the truth.",
        "Connect the dots, people. It's all right in front of us.",
    ]
    
    content = f"{random.choice(intro_templates)}\n\n"
    content += f"{random.choice(middle_templates)}\n\n"
    content += f"{fake.paragraph(nb_sentences=random.randint(3, 8))}\n\n"
    content += random.choice(conclusion_templates)
    
    return content


def generate_comment_content():
    """Generate a comment for a conspiracy theory."""
    templates = [
        "This is exactly what I've been saying for years!",
        "Finally, someone who gets it!",
        f"I knew it! {random.choice(CONSPIRACY_SUBJECTS)} can't hide this forever.",
        "My cousin works at [REDACTED] and confirmed this is all true.",
        "They're going to try to silence you for posting this.",
        "This goes even deeper than you think...",
        "I have more evidence that supports this. DM me.",
        "Wake up people! How can you not see this?",
        "Interesting theory, but have you considered the connection to " + random.choice(CONSPIRACY_TOPICS) + "?",
        fake.paragraph(nb_sentences=random.randint(1, 3)),
        "The shills are going to attack you for this, but stay strong.",
        "I've been researching this for years. You're onto something big.",
        "This is why I don't trust " + random.choice(CONSPIRACY_SUBJECTS).lower() + ".",
        "Follow the money. It always leads back to " + random.choice(CONSPIRACY_SUBJECTS).lower() + ".",
        "My neighbor saw something related to this. I'll ask them for details.",
        "This needs more attention. Sharing everywhere!",
        f"The connection between {random.choice(CONSPIRACY_TOPICS)} and {random.choice(CONSPIRACY_TOPICS)} is undeniable.",
        "Sources: trust me bro. But seriously, this is well-documented.",
        "I want to believe, but do you have any hard evidence?",
        "Skeptic here - this actually made me think twice.",
    ]
    return random.choice(templates)


def generate_username():
    """Generate a conspiracy-themed username."""
    prefixes = [
        "truth", "seeker", "woke", "awake", "aware", "real", "hidden", "shadow",
        "free", "red_pilled", "enlightened", "insider", "whistleblower", "patriot"
    ]
    suffixes = [
        "hunter", "finder", "exposer", "revealer", "warrior", "knight",
        "seeker", "watcher", "guardian", "detective"
    ]
    
    patterns = [
        f"{random.choice(prefixes)}_{fake.user_name()}",
        f"{fake.user_name()}_{random.randint(1, 999)}",
        f"{random.choice(prefixes)}{random.choice(suffixes)}{random.randint(1, 99)}",
        f"the_real_{fake.first_name().lower()}",
        f"{fake.first_name().lower()}_{random.choice(suffixes)}",
        fake.user_name(),
    ]
    return random.choice(patterns)[:50]  # Ensure username fits in VARCHAR(255)


def generate_evidence_urls(count: int = None):
    """Generate fake evidence URLs."""
    if count is None:
        count = random.randint(0, 5)
    
    domains = [
        "truth-exposed.com", "wake-up-world.net", "hidden-knowledge.org",
        "real-news-daily.com", "conspiracy-facts.info", "declassified-docs.net",
        "whistleblower-leaks.org", "underground-truth.com", "the-real-story.net"
    ]
    
    urls = []
    for _ in range(count):
        domain = random.choice(domains)
        path = fake.slug()
        urls.append(f"https://{domain}/{path}")
    
    return urls


def random_datetime(start_days_ago=365, end_days_ago=0):
    """Generate a random datetime within the specified range."""
    start = datetime.now() - timedelta(days=start_days_ago)
    end = datetime.now() - timedelta(days=end_days_ago)
    delta = end - start
    random_seconds = random.randint(0, int(delta.total_seconds()))
    return start + timedelta(seconds=random_seconds)


def populate_users(cursor):
    """Generate and insert 100 users."""
    print(f"Generating {NUM_USERS} users...")
    
    # Common password for all generated users (password123)
    hashed_password = hash_password("password123")
    
    users = []
    used_usernames = set()
    used_emails = set()
    
    for i in range(NUM_USERS):
        # Generate unique username
        username = generate_username()
        while username in used_usernames:
            username = generate_username()
        used_usernames.add(username)
        
        # Generate unique email
        email = fake.unique.email()
        while email in used_emails:
            email = fake.unique.email()
        used_emails.add(email)
        
        # 5% chance of being admin
        role = 'ADMIN' if random.random() < 0.05 else 'USER'
        
        # 20% chance of having a secret code
        secret_code = fake.password(length=12) if random.random() < 0.2 else None
        
        # 15% chance of anonymous mode
        is_anonymous = random.random() < 0.15
        
        # Random reputation between -50 and 500
        reputation = random.randint(-50, 500)
        
        # Random creation date within the last year
        created_at = random_datetime(365, 1)
        
        # 10% chance of must_change_password
        must_change_password = random.random() < 0.1
        
        users.append((
            username,
            hashed_password,
            email,
            secret_code,
            role,
            is_anonymous,
            reputation,
            created_at,
            must_change_password
        ))
    
    insert_query = """
        INSERT INTO users (username, password, email, secret_code, role, is_anonymous, reputation, created_at, must_change_password)
        VALUES %s
        RETURNING id
    """
    
    user_ids = execute_values(cursor, insert_query, users, fetch=True)
    user_ids = [row[0] for row in user_ids]
    print(f"Inserted {len(user_ids)} users")
    return user_ids


def populate_theories(cursor, user_ids):
    """Generate and insert 100 theories."""
    print(f"Generating {NUM_THEORIES} theories...")
    
    theories = []
    
    for i in range(NUM_THEORIES):
        title = generate_conspiracy_title()[:255]  # Ensure fits in VARCHAR(255)
        content = generate_conspiracy_content()
        status = random.choice(THEORY_STATUSES)
        posted_at = random_datetime(180, 1)
        updated_at = posted_at + timedelta(hours=random.randint(0, 72))
        is_anonymous_post = random.random() < 0.25  # 25% anonymous
        author_id = random.choice(user_ids)
        comment_count = 0  # Will be updated after comments are inserted
        score = random.randint(-20, 100)
        
        theories.append((
            title,
            content,
            status,
            posted_at,
            updated_at,
            is_anonymous_post,
            author_id,
            comment_count,
            score
        ))
    
    insert_query = """
        INSERT INTO theories (title, content, status, posted_at, updated_at, is_anonymous_post, author_id, comment_count, score)
        VALUES %s
        RETURNING id
    """
    
    theory_ids = execute_values(cursor, insert_query, theories, fetch=True)
    theory_ids = [row[0] for row in theory_ids]
    print(f"Inserted {len(theory_ids)} theories")
    return theory_ids


def populate_evidence_urls(cursor, theory_ids):
    """Generate and insert evidence URLs for theories."""
    print("Generating evidence URLs for theories...")
    
    evidence_entries = []
    
    for theory_id in theory_ids:
        urls = generate_evidence_urls()
        for url in urls:
            evidence_entries.append((theory_id, url[:255]))
    
    if evidence_entries:
        insert_query = """
            INSERT INTO theory_evidence_urls (theory_id, url)
            VALUES %s
        """
        execute_values(cursor, insert_query, evidence_entries)
        print(f"Inserted {len(evidence_entries)} evidence URLs")


def populate_comments(cursor, user_ids, theory_ids):
    """Generate and insert 100 comments."""
    print(f"Generating {NUM_COMMENTS} comments...")
    
    comments = []
    
    for i in range(NUM_COMMENTS):
        content = generate_comment_content()
        posted_at = random_datetime(90, 0)
        updated_at = posted_at + timedelta(hours=random.randint(0, 24))
        is_anonymous_post = random.random() < 0.2  # 20% anonymous
        author_id = random.choice(user_ids)
        theory_id = random.choice(theory_ids)
        parent_id = None  # First pass: all top-level comments
        score = random.randint(-10, 50)
        
        comments.append((
            content,
            posted_at,
            updated_at,
            is_anonymous_post,
            author_id,
            theory_id,
            parent_id,
            score
        ))
    
    insert_query = """
        INSERT INTO comments (content, posted_at, updated_at, is_anonymous_post, author_id, theory_id, parent_id, score)
        VALUES %s
        RETURNING id, theory_id
    """
    
    result = execute_values(cursor, insert_query, comments, fetch=True)
    comment_ids = [row[0] for row in result]
    
    # Update comment counts for theories
    cursor.execute("""
        UPDATE theories t
        SET comment_count = (
            SELECT COUNT(*) FROM comments c WHERE c.theory_id = t.id
        )
    """)
    
    print(f"Inserted {len(comment_ids)} comments")
    return comment_ids


def populate_replies(cursor, user_ids, theory_ids, comment_ids):
    """Add some reply comments (nested comments)."""
    print("Generating reply comments...")
    
    num_replies = min(30, len(comment_ids))  # Add up to 30 replies
    replies = []
    
    # Get comment-theory mappings
    cursor.execute("SELECT id, theory_id FROM comments")
    comment_theory_map = {row[0]: row[1] for row in cursor.fetchall()}
    
    for i in range(num_replies):
        parent_id = random.choice(comment_ids)
        theory_id = comment_theory_map.get(parent_id)
        
        if theory_id is None:
            continue
            
        content = generate_comment_content()
        posted_at = random_datetime(30, 0)
        updated_at = posted_at + timedelta(hours=random.randint(0, 12))
        is_anonymous_post = random.random() < 0.2
        author_id = random.choice(user_ids)
        score = random.randint(-5, 30)
        
        replies.append((
            content,
            posted_at,
            updated_at,
            is_anonymous_post,
            author_id,
            theory_id,
            parent_id,
            score
        ))
    
    if replies:
        insert_query = """
            INSERT INTO comments (content, posted_at, updated_at, is_anonymous_post, author_id, theory_id, parent_id, score)
            VALUES %s
        """
        execute_values(cursor, insert_query, replies)
        
        # Update comment counts again
        cursor.execute("""
            UPDATE theories t
            SET comment_count = (
                SELECT COUNT(*) FROM comments c WHERE c.theory_id = t.id
            )
        """)
        
        print(f"Inserted {len(replies)} reply comments")


def populate_votes(cursor, user_ids, theory_ids, comment_ids):
    """Generate and insert 100 votes (mix of theory and comment votes)."""
    print(f"Generating {NUM_VOTES} votes...")
    
    votes = []
    used_combinations = set()
    
    # Generate theory votes (60% of votes)
    num_theory_votes = int(NUM_VOTES * 0.6)
    theory_votes_added = 0
    attempts = 0
    max_attempts = NUM_VOTES * 10
    
    while theory_votes_added < num_theory_votes and attempts < max_attempts:
        attempts += 1
        user_id = random.choice(user_ids)
        theory_id = random.choice(theory_ids)
        
        combo = (user_id, 'theory', theory_id)
        if combo in used_combinations:
            continue
        
        used_combinations.add(combo)
        vote_value = random.choice([-1, 1])  # Upvote or downvote
        created_at = random_datetime(60, 0)
        
        votes.append((user_id, theory_id, None, vote_value, created_at))
        theory_votes_added += 1
    
    # Generate comment votes (40% of votes)
    num_comment_votes = NUM_VOTES - theory_votes_added
    comment_votes_added = 0
    attempts = 0
    
    while comment_votes_added < num_comment_votes and attempts < max_attempts:
        attempts += 1
        user_id = random.choice(user_ids)
        comment_id = random.choice(comment_ids)
        
        combo = (user_id, 'comment', comment_id)
        if combo in used_combinations:
            continue
        
        used_combinations.add(combo)
        vote_value = random.choice([-1, 1])
        created_at = random_datetime(60, 0)
        
        votes.append((user_id, None, comment_id, vote_value, created_at))
        comment_votes_added += 1
    
    if votes:
        insert_query = """
            INSERT INTO votes (user_id, theory_id, comment_id, vote_value, created_at)
            VALUES %s
        """
        execute_values(cursor, insert_query, votes)
    
    print(f"Inserted {len(votes)} votes ({theory_votes_added} theory votes, {comment_votes_added} comment votes)")


def update_scores(cursor):
    """Update scores for theories and comments based on votes."""
    print("Updating scores based on votes...")
    
    # Update theory scores
    cursor.execute("""
        UPDATE theories t
        SET score = COALESCE((
            SELECT SUM(vote_value) FROM votes v WHERE v.theory_id = t.id
        ), 0)
    """)
    
    # Update comment scores
    cursor.execute("""
        UPDATE comments c
        SET score = COALESCE((
            SELECT SUM(vote_value) FROM votes v WHERE v.comment_id = c.id
        ), 0)
    """)
    
    print("Scores updated")


def check_existing_data(cursor):
    """Check if there's already data beyond the initial seed data."""
    cursor.execute("SELECT COUNT(*) FROM users")
    user_count = cursor.fetchone()[0]
    
    # The init.sql adds 4 users, so if we have more, data was already populated
    if user_count > 10:
        return True
    return False


def main():
    """Main function to populate the database."""
    print("=" * 60)
    print("ConspiracyCorner Database Population Script")
    print("=" * 60)
    
    # Wait for database to be ready
    wait_for_db()
    
    # Connect to database
    conn = psycopg2.connect(**DB_CONFIG)
    conn.autocommit = False
    cursor = conn.cursor()
    
    try:
        # Check if data already exists
        if check_existing_data(cursor):
            print("Database already contains populated data. Skipping population.")
            print("To repopulate, clear the database first.")
            return
        
        print("\nStarting data population...")
        print("-" * 40)
        
        # Populate in order of dependencies
        user_ids = populate_users(cursor)
        theory_ids = populate_theories(cursor, user_ids)
        populate_evidence_urls(cursor, theory_ids)
        comment_ids = populate_comments(cursor, user_ids, theory_ids)
        populate_replies(cursor, user_ids, theory_ids, comment_ids)
        populate_votes(cursor, user_ids, theory_ids, comment_ids)
        update_scores(cursor)
        
        # Commit all changes
        conn.commit()
        
        print("-" * 40)
        print("Data population completed successfully!")
        print("=" * 60)
        
        # Print summary
        cursor.execute("SELECT COUNT(*) FROM users")
        print(f"Total users: {cursor.fetchone()[0]}")
        
        cursor.execute("SELECT COUNT(*) FROM theories")
        print(f"Total theories: {cursor.fetchone()[0]}")
        
        cursor.execute("SELECT COUNT(*) FROM comments")
        print(f"Total comments: {cursor.fetchone()[0]}")
        
        cursor.execute("SELECT COUNT(*) FROM votes")
        print(f"Total votes: {cursor.fetchone()[0]}")
        
        cursor.execute("SELECT COUNT(*) FROM theory_evidence_urls")
        print(f"Total evidence URLs: {cursor.fetchone()[0]}")
        
    except Exception as e:
        conn.rollback()
        print(f"Error during population: {e}")
        raise
    finally:
        cursor.close()
        conn.close()


if __name__ == "__main__":
    main()
