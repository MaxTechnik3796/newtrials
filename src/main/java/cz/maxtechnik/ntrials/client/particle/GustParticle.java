package cz.maxtechnik.ntrials.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GustParticle extends TextureSheetParticle {

    protected GustParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        super(level, x, y, z);
        this.setSize(0.2F, 0.2F);

        // Increased base size and variation for better visibility
        this.quadSize = 0.5F + this.random.nextFloat() * 0.5F;

        // Enhanced movement with more noticeable velocity
        this.xd = xSpeed + (Math.random() * 2.0D - 1.0D) * 0.1D;
        this.yd = ySpeed + (Math.random() * 2.0D - 1.0D) * 0.1D;
        this.zd = zSpeed + (Math.random() * 2.0D - 1.0D) * 0.1D;

        // Longer lifetime for better visibility
        this.lifetime = 20 + this.random.nextInt(10);

        // No gravity for wind effect
        this.gravity = 0.0F;

        // Less friction for smoother movement
        this.friction = 0.92F;

        // Full opacity initially
        this.alpha = 1.0F;

        // White color for visibility
        this.rCol = 1.0F;
        this.gCol = 1.0F;
        this.bCol = 1.0F;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void tick() {
        super.tick();

        // Gradual fade out
        this.alpha = Math.max(0.0F, 1.0F - ((float)this.age / (float)this.lifetime));

        // Gradual size increase for expansion effect
        this.quadSize *= 1.05F;

        // Add some upward drift
        this.yd += 0.02D;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public Provider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType particleType, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            GustParticle gustParticle = new GustParticle(level, x, y, z, xSpeed, ySpeed, zSpeed);
            gustParticle.pickSprite(this.sprite);
            return gustParticle;
        }
    }
}
