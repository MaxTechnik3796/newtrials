package cz.maxtechnik.ntrials.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
@OnlyIn(Dist.CLIENT)
public class SmallGustParticle extends TextureSheetParticle{
	protected SmallGustParticle(ClientLevel level,double x,double y,double z,double xSpeed,double ySpeed,double zSpeed){
		super(level,x,y,z);
		this.setSize(0.1F,0.1F);
		// Smaller but still visible size
		this.quadSize=0.3F+this.random.nextFloat()*0.2F;
		// Enhanced movement
		this.xd=xSpeed+(Math.random()*2.0D-1.0D)*0.05D;
		this.yd=ySpeed+(Math.random()*2.0D-1.0D)*0.05D;
		this.zd=zSpeed+(Math.random()*2.0D-1.0D)*0.05D;
		// Medium lifetime
		this.lifetime=15+this.random.nextInt(10);
		// No gravity
		this.gravity=0.0F;
		// Smooth movement
		this.friction=0.94F;
		// Full opacity initially
		this.alpha=1.0F;
		// Light gray/white color for visibility
		this.rCol=0.9F;
		this.gCol=0.9F;
		this.bCol=1.0F;
	}
	@Override
	public @NotNull ParticleRenderType getRenderType(){
		return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
	}
	@Override
	public void tick(){
		super.tick();
		// Gradual fade out
		this.alpha=Math.max(0.0F,1.0F-((float)this.age/(float)this.lifetime));
		// Slight size increase
		this.quadSize*=1.02F;
		// Add some swirling motion
		this.yd+=0.01D;
	}
	@OnlyIn(Dist.CLIENT)
	public record Provider(SpriteSet sprite) implements ParticleProvider<SimpleParticleType>{
		@Override
		public Particle createParticle(@NotNull SimpleParticleType particleType,@NotNull ClientLevel level,double x,double y,double z,double xSpeed,double ySpeed,double zSpeed){
			SmallGustParticle gustParticle=new SmallGustParticle(level,x,y,z,xSpeed,ySpeed,zSpeed);
			gustParticle.pickSprite(this.sprite);
			return gustParticle;
		}
	}
}
